#!/usr/bin/env python3
"""
Decoder for protobuf-lite's GeneratedMessageLite.newMessageInfo(default, infoString, objects)
compact schema-string format ("RawMessageInfo" codegen style).

Ported, field-for-field, from the real upstream sources (not reconstructed from memory):
  - com/google/protobuf/RawMessageInfo.java        (info-string char-decoding + header flags)
  - com/google/protobuf/MessageSchema.java          (newSchemaForRawMessageInfo: the full field-entry
                                                      parsing algorithm, constants, and objects-array
                                                      consumption order)
  - com/google/protobuf/FieldType.java              (field-type id table, values 0-50)
fetched 2026-08-30 from https://github.com/protocolbuffers/protobuf (main branch) and saved
alongside this script for reference (RawMessageInfo.java, MessageSchema.java, FieldType.java).

This script does NOT guess or approximate the encoding - every constant below is copied from
those files. See REVERSE_ENGINEERING.md for the project rules this supports (PROJECT_RULES.md §1:
"operate with zero creativity when parsing... never from a plausible-sounding guess").
"""
import re
import sys
import json
from pathlib import Path

# ---- constants, copied verbatim from MessageSchema.java ----
INTS_PER_FIELD = 3
OFFSET_BITS = 20
OFFSET_MASK = (1 << OFFSET_BITS) - 1
REQUIRED_MASK = 0x10000000
ENFORCE_UTF8_MASK = 0x20000000
LEGACY_ENUM_IS_CLOSED_MASK = 0x80000000

REQUIRED_BIT = 0x100
UTF8_CHECK_BIT = 0x200
CHECK_INITIALIZED_BIT = 0x400
LEGACY_ENUM_IS_CLOSED_BIT = 0x800
HAS_HAS_BIT = 0x1000

ONEOF_TYPE_OFFSET = 51  # FieldType.MAP(50) + 1

# ---- FieldType id table, copied verbatim from FieldType.java ----
FIELD_TYPE_NAMES = {
    0: "DOUBLE", 1: "FLOAT", 2: "INT64", 3: "UINT64", 4: "INT32", 5: "FIXED64",
    6: "FIXED32", 7: "BOOL", 8: "STRING", 9: "MESSAGE", 10: "BYTES", 11: "UINT32",
    12: "ENUM", 13: "SFIXED32", 14: "SFIXED64", 15: "SINT32", 16: "SINT64", 17: "GROUP",
    18: "DOUBLE_LIST", 19: "FLOAT_LIST", 20: "INT64_LIST", 21: "UINT64_LIST", 22: "INT32_LIST",
    23: "FIXED64_LIST", 24: "FIXED32_LIST", 25: "BOOL_LIST", 26: "STRING_LIST", 27: "MESSAGE_LIST",
    28: "BYTES_LIST", 29: "UINT32_LIST", 30: "ENUM_LIST", 31: "SFIXED32_LIST", 32: "SFIXED64_LIST",
    33: "SINT32_LIST", 34: "SINT64_LIST", 35: "DOUBLE_LIST_PACKED", 36: "FLOAT_LIST_PACKED",
    37: "INT64_LIST_PACKED", 38: "UINT64_LIST_PACKED", 39: "INT32_LIST_PACKED",
    40: "FIXED64_LIST_PACKED", 41: "FIXED32_LIST_PACKED", 42: "BOOL_LIST_PACKED",
    43: "UINT32_LIST_PACKED", 44: "ENUM_LIST_PACKED", 45: "SFIXED32_LIST_PACKED",
    46: "SFIXED64_LIST_PACKED", 47: "SINT32_LIST_PACKED", 48: "SINT64_LIST_PACKED",
    49: "GROUP_LIST", 50: "MAP",
}


def read_varint_chars(info, i):
    """Mirrors RawMessageInfo's/MessageSchema's char->int decoding exactly (1-3 UTF-16 chars)."""
    next_ = ord(info[i]); i += 1
    if next_ >= 0xD800:
        result = next_ & 0x1FFF
        shift = 13
        while True:
            next_ = ord(info[i]); i += 1
            if next_ < 0xD800:
                break
            result |= (next_ & 0x1FFF) << shift
            shift += 13
        next_ = result | (next_ << shift)
    return next_, i


def decode_info_string(info, objects):
    """Full port of MessageSchema.newSchemaForRawMessageInfo's field-entry parsing loop."""
    length = len(info)
    i = 0

    unused_flags, i = read_varint_chars(info, i)
    field_count, i = read_varint_chars(info, i)

    if field_count == 0:
        return {
            "unused_flags": unused_flags, "field_count": 0, "fields": [],
            "oneof_count": 0, "has_bits_count": 0, "min_field_number": 0,
            "max_field_number": 0, "map_field_count": 0,
        }

    oneof_count, i = read_varint_chars(info, i)
    has_bits_count, i = read_varint_chars(info, i)
    min_field_number, i = read_varint_chars(info, i)
    max_field_number, i = read_varint_chars(info, i)
    num_entries, i = read_varint_chars(info, i)
    map_field_count, i = read_varint_chars(info, i)
    _repeated_field_count, i = read_varint_chars(info, i)  # unused by MessageSchema itself
    check_initialized, i = read_varint_chars(info, i)

    objects_position = oneof_count * 2 + has_bits_count

    fields = []
    oneof_names = {}  # oneofIndex -> (value_field_obj, case_field_obj) source text

    while i < length:
        field_number, i = read_varint_chars(info, i)
        field_type_with_extra, i = read_varint_chars(info, i)
        field_type = field_type_with_extra & 0xFF

        entry = {
            "field_number": field_number,
            "raw_type_value": field_type,
            "required": bool(field_type_with_extra & REQUIRED_BIT),
            "check_utf8": bool(field_type_with_extra & UTF8_CHECK_BIT),
            "needs_is_initialized_check": bool(field_type_with_extra & CHECK_INITIALIZED_BIT),
            "legacy_enum_closed_or_map_enum": bool(field_type_with_extra & 0x0800),
            "supports_presence": bool(field_type_with_extra & HAS_HAS_BIT),
        }

        if field_type >= ONEOF_TYPE_OFFSET:
            oneof_index, i = read_varint_chars(info, i)
            oneof_field_type = field_type - ONEOF_TYPE_OFFSET
            entry["is_oneof"] = True
            entry["oneof_index"] = oneof_index
            entry["type_name"] = FIELD_TYPE_NAMES.get(oneof_field_type, f"UNKNOWN({oneof_field_type})")
            entry["oneof_field_type_id"] = oneof_field_type

            if oneof_field_type in (9, 17):  # MESSAGE, GROUP
                obj = objects[objects_position] if objects_position < len(objects) else None
                entry["message_class_ref"] = obj
                objects_position += 1
            elif oneof_field_type == 12:  # ENUM
                # legacy proto2 check omitted here (we don't have a live ProtoSyntax value);
                # LEGACY_ENUM_IS_CLOSED_BIT presence is what this decoder can check statically
                if field_type_with_extra & LEGACY_ENUM_IS_CLOSED_BIT:
                    obj = objects[objects_position] if objects_position < len(objects) else None
                    entry["enum_map_ref"] = obj
                    objects_position += 1

            if oneof_index not in oneof_names:
                idx = oneof_index * 2
                value_field = objects[idx] if idx < len(objects) else None
                case_field = objects[idx + 1] if idx + 1 < len(objects) else None
                oneof_names[oneof_index] = (value_field, case_field)
            entry["oneof_value_field"], entry["oneof_case_field"] = oneof_names[oneof_index]

        else:
            entry["is_oneof"] = False
            entry["type_name"] = FIELD_TYPE_NAMES.get(field_type, f"UNKNOWN({field_type})")
            field_name_obj = objects[objects_position] if objects_position < len(objects) else None
            entry["java_field"] = field_name_obj
            objects_position += 1

            if field_type in (9, 17):  # MESSAGE, GROUP -- consumes no extra object (field.getType())
                pass
            elif field_type in (27, 49):  # MESSAGE_LIST, GROUP_LIST
                obj = objects[objects_position] if objects_position < len(objects) else None
                entry["message_class_ref"] = obj
                objects_position += 1
            elif field_type in (12, 30, 44):  # ENUM, ENUM_LIST, ENUM_LIST_PACKED
                if field_type_with_extra & LEGACY_ENUM_IS_CLOSED_BIT:
                    obj = objects[objects_position] if objects_position < len(objects) else None
                    entry["enum_map_ref"] = obj
                    objects_position += 1
            elif field_type == 50:  # MAP
                obj = objects[objects_position] if objects_position < len(objects) else None
                entry["map_default_entry_ref"] = obj
                objects_position += 1
                if field_type_with_extra & LEGACY_ENUM_IS_CLOSED_BIT:
                    obj2 = objects[objects_position] if objects_position < len(objects) else None
                    entry["map_enum_map_ref"] = obj2
                    objects_position += 1

            has_has_bit = bool(field_type_with_extra & HAS_HAS_BIT)
            if has_has_bit and field_type <= 17:
                hasbits_index, i = read_varint_chars(info, i)
                entry["hasbits_index"] = hasbits_index

        fields.append(entry)

    return {
        "unused_flags": unused_flags,
        "field_count": field_count,
        "oneof_count": oneof_count,
        "has_bits_count": has_bits_count,
        "min_field_number": min_field_number,
        "max_field_number": max_field_number,
        "map_field_count": map_field_count,
        "check_initialized": check_initialized,
        "fields": fields,
    }


# ---------------------------------------------------------------------------
# Java-source-level extraction: find `new naa(<default>, "<info>", <objects>)`
# inside a decompiled .java file (case 2 of the obfuscated dynamicMethod/newMessageInfo
# dispatch, per REVERSE_ENGINEERING.md's confirmed naa/myp mapping) and pull out the
# raw string-literal + object-array-literal source text.
# ---------------------------------------------------------------------------

def unescape_java_string(lit: str) -> str:
    """Unescape a Java string literal body (without the surrounding quotes)."""
    out = []
    i = 0
    n = len(lit)
    while i < n:
        c = lit[i]
        if c == '\\' and i + 1 < n:
            nc = lit[i + 1]
            if nc == 'u':
                hex4 = lit[i + 2:i + 6]
                out.append(chr(int(hex4, 16)))
                i += 6
                continue
            simple = {'n': '\n', 't': '\t', 'r': '\r', 'b': '\b', 'f': '\f',
                      '"': '"', "'": "'", '\\': '\\', '0': '\0'}
            if nc in simple:
                out.append(simple[nc])
                i += 2
                continue
            if nc.isdigit():
                m = re.match(r'[0-7]{1,3}', lit[i + 1:])
                out.append(chr(int(m.group(0), 8)))
                i += 1 + len(m.group(0))
                continue
            out.append(nc)
            i += 2
            continue
        out.append(c)
        i += 1
    return ''.join(out)


def find_string_literal(src: str, start: int):
    """src[start] must be '"'. Returns (raw_literal_body, index_after_closing_quote)."""
    assert src[start] == '"'
    i = start + 1
    buf = []
    while True:
        c = src[i]
        if c == '\\':
            buf.append(c)
            buf.append(src[i + 1])
            i += 2
            continue
        if c == '"':
            return ''.join(buf), i + 1
        buf.append(c)
        i += 1


def split_top_level_args(src: str, start: int):
    """src[start] must be '('. Returns (list_of_arg_source_strings, index_after_closing_paren)."""
    assert src[start] == '('
    i = start + 1
    depth = 1
    args = []
    cur = []
    while depth > 0:
        c = src[i]
        if c == '"':
            _, after = find_string_literal(src, i)
            cur.append(src[i:after])
            i = after
            continue
        if c in '([{':
            depth += 1
            cur.append(c)
            i += 1
            continue
        if c in ')]}':
            depth -= 1
            if depth == 0:
                break
            cur.append(c)
            i += 1
            continue
        if c == ',' and depth == 1:
            args.append(''.join(cur).strip())
            cur = []
            i += 1
            continue
        cur.append(c)
        i += 1
    if ''.join(cur).strip():
        args.append(''.join(cur).strip())
    return args, i + 1


def split_top_level_elements(src: str):
    """Split a comma-separated Java literal body (already stripped of outer braces) into
    top-level element source strings, respecting nesting/strings."""
    i = 0
    n = len(src)
    depth = 0
    cur = []
    elems = []
    while i < n:
        c = src[i]
        if c == '"':
            _, after = find_string_literal(src, i)
            cur.append(src[i:after])
            i = after
            continue
        if c in '([{':
            depth += 1
            cur.append(c)
            i += 1
            continue
        if c in ')]}':
            depth -= 1
            cur.append(c)
            i += 1
            continue
        if c == ',' and depth == 0:
            elems.append(''.join(cur).strip())
            cur = []
            i += 1
            continue
        cur.append(c)
        i += 1
    tail = ''.join(cur).strip()
    if tail:
        elems.append(tail)
    return elems


def parse_objects_arg(arg_src: str):
    """arg_src is the raw source text of the 3rd `new naa(...)` argument: either `null`
    or something like `new Object[]{"c", "b", qhx.class, ...}`. Returns a list of Python
    values: unescaped strings for string literals, or the raw source text otherwise
    (e.g. 'qhx.class')."""
    arg_src = arg_src.strip()
    if arg_src == 'null':
        return []
    m = re.match(r'new\s+Object\[\]\s*\{(.*)\}$', arg_src, re.DOTALL)
    if not m:
        # Fallback: sometimes JADX may just print `new Object[]{...}` differently, or
        # occasionally emits a bare array without `new Object[]` prefix -- try to find the
        # outermost {...}
        b = arg_src.find('{')
        e = arg_src.rfind('}')
        if b == -1 or e == -1:
            raise ValueError(f"cannot parse objects arg: {arg_src!r}")
        body = arg_src[b + 1:e]
    else:
        body = m.group(1)
    elems = split_top_level_elements(body)
    out = []
    for el in elems:
        el = el.strip()
        if el.startswith('"') and el.endswith('"'):
            out.append(unescape_java_string(el[1:-1]))
        else:
            out.append(el)  # raw source, e.g. "qhx.class" or "a"
    return out


def find_naa_constructions(src: str):
    """Find every `new naa(` call in the source and return parsed (default_src, info_string,
    objects_list, start_offset) tuples."""
    results = []
    for m in re.finditer(r'new naa\(', src):
        paren_idx = m.end() - 1
        args, _end = split_top_level_args(src, paren_idx)
        if len(args) != 3:
            continue
        default_src, info_arg, objects_arg = args
        if not (info_arg.startswith('"') and info_arg.endswith('"')):
            continue
        info_string = unescape_java_string(info_arg[1:-1])
        try:
            objects = parse_objects_arg(objects_arg)
        except ValueError:
            objects = None
        results.append({
            "default_src": default_src,
            "info_string_literal": info_arg,
            "info_string": info_string,
            "objects_src": objects_arg,
            "objects": objects,
            "offset": m.start(),
        })
    return results


def decode_file(path: Path):
    src = path.read_text(encoding='utf-8', errors='replace')
    constructions = find_naa_constructions(src)
    out = []
    for c in constructions:
        if c["objects"] is None:
            continue
        decoded = decode_info_string(c["info_string"], c["objects"])
        out.append({
            "file": str(path),
            "default_src": c["default_src"],
            "objects_src_preview": c["objects_src"][:300],
            "decoded": decoded,
        })
    return out


def summarize(decoded_entry, class_name):
    d = decoded_entry["decoded"]
    lines = [f"{class_name}: {d['field_count']} field(s), {d.get('oneof_count', 0)} oneof(s), "
             f"{d.get('map_field_count', 0)} map field(s), fieldNum range "
             f"[{d.get('min_field_number', 0)}-{d.get('max_field_number', 0)}]"]
    for f in d["fields"]:
        if f.get("is_oneof"):
            ref = f.get("message_class_ref") or f.get("enum_map_ref") or ""
            lines.append(f"  field {f['field_number']}: ONEOF({f['type_name']}) "
                          f"oneof_index={f['oneof_index']} value_field={f.get('oneof_value_field')!r} "
                          f"case_field={f.get('oneof_case_field')!r} ref={ref}")
        else:
            extra = []
            if "message_class_ref" in f:
                extra.append(f"msg_ref={f['message_class_ref']}")
            if "map_default_entry_ref" in f:
                extra.append(f"map_default={f['map_default_entry_ref']}")
            if "hasbits_index" in f:
                extra.append(f"hasbit={f['hasbits_index']}")
            if f.get("supports_presence"):
                extra.append("has_presence")
            extra_s = (" " + " ".join(extra)) if extra else ""
            lines.append(f"  field {f['field_number']}: {f['type_name']} "
                          f"java_field={f.get('java_field')!r}{extra_s}")
    return "\n".join(lines)


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("usage: decode_rawmessageinfo.py <file.java> [file2.java ...]")
        sys.exit(1)
    for fp in sys.argv[1:]:
        p = Path(fp)
        class_name = p.stem
        results = decode_file(p)
        if not results:
            print(f"{class_name}: no `new naa(...)` construction found / objects unparsable")
            continue
        for r in results:
            print(summarize(r, class_name))
            print()
