import re, sys, zlib
txt = open(sys.argv[1]).read()
frames = []
cur = None
for line in txt.splitlines():
    if line.startswith('--- Frame'):
        cur = []; frames.append((line, cur)); continue
    if cur is not None and re.fullmatch(r'([0-9a-f]{2} ?)+', line.strip()):
        cur += [int(b, 16) for b in line.split()]
def varint(b, i):
    v = s = 0
    while True:
        x = b[i]; i += 1; v |= (x & 0x7f) << s; s += 7
        if not x & 0x80: return v, i
def fields(b):
    i = 0; out = []
    while i < len(b):
        tag, i = varint(b, i); f, wt = tag >> 3, tag & 7
        if wt == 0: v, i = varint(b, i)
        elif wt == 1: v = int.from_bytes(b[i:i+8], 'little'); i += 8
        elif wt == 5: v = int.from_bytes(b[i:i+4], 'little'); i += 4
        elif wt == 2: n, i = varint(b, i); v = bytes(b[i:i+n]); i += n
        else: raise ValueError(wt)
        out.append((f, wt, v))
    return out
PEERS = {1:'Host',2:'Case',3:'LeftBtCore',4:'RightBtCore',5:'LSensorHub',6:'RSensorHub',10:'MaestroA',13:'MaestroB'}
CH = {(10,2):18,(10,3):19,(10,5):20,(10,4):21,(10,6):22,(13,2):23,(13,3):24,(13,5):25,(13,4):26,(13,6):27}
def h(s): # pigweed 65599 hash
    r = len(s); c = 65599
    for ch in s.encode(): r = (r + c*ch) & 0xffffffff; c = (c*65599) & 0xffffffff
    return r
names = {h(n): n for n in ['maestro_pw.Maestro','GetSoftwareInfo','GetHardwareInfo','SubscribeRuntimeInfo','WriteSetting','ReadSetting','SubscribeToSettingsChanges','SubscribeToOobeActions','maestro_pw.Multipoint','maestro_pw.Dosimeter','SubscribeToQuietModeStatus']}
TYPES = {0:'REQUEST',1:'RESPONSE',2:'CLIENT_STREAM',4:'CLIENT_ERROR',5:'SERVER_ERROR',7:'SERVER_STREAM'}
for title, raw in frames:
    body = raw[1:-1]; un = []; esc = False
    for x in body:
        if esc: un.append(x ^ 0x20); esc = False
        elif x == 0x7d: esc = True
        else: un.append(x)
    data, crc = un[:-4], int.from_bytes(bytes(un[-4:]), 'little')
    crc_ok = zlib.crc32(bytes(data)) == crc
    # one-terminated address varint
    addr = 0; s = 0; i = 0
    while True:
        x = data[i]; i += 1; addr |= (x >> 1) << s; s += 7
        if x & 1: break
    ctrl = data[i]; pkt = data[i+1:]
    src, dst = (addr >> 6) & 15, (addr >> 10) & 15
    local, remote = (src, dst) if src in (10, 13) else (dst, src)
    print(title.strip()[:40], f'| crc_ok={crc_ok} addr={addr} {PEERS.get(src,src)}->{PEERS.get(dst,dst)} ch={CH.get((local,remote))} ctrl={ctrl:#x}')
    for f, wt, v in fields(pkt):
        if f == 1: print('   type', TYPES.get(v, v))
        elif f == 2: print('   channel_id', v)
        elif f in (3, 4): print('  ', 'service' if f == 3 else 'method', hex(v), names.get(v, '?'))
        elif f == 7: print('   call_id', hex(v))
        elif f == 6: print('   status', v)
        elif f == 5:
            print('   payload', v.hex(' '))
            try:
                for ff in fields(v): print('     ', ff[0], ff[1], ff[2].hex(' ') if isinstance(ff[2], bytes) else ff[2])
            except Exception as e: print('      (not plain proto)', e)
        else: print('   field', f, v)
