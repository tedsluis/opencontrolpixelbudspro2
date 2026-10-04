#!/usr/bin/env python3
"""Message-level tally of the Fast Pair Message Stream in every capture log.

PROJECT_RULES.md rule 4a: the tool behind a count is committed. Written in ai-sessions/0069 (0068 A68-PROT-05, A68-CAP-14).

The Message Stream is RFCOMM server channel 2: DLCI 4 when the phone opened the multiplexer, DLCI 5 when the Buds did
(PROTOCOL.md 2.3). Frames are reassembled per (direction, connection handle, DLCI) into [Group][Code][Len:2 BE][Value]
messages, so a message split over two RFCOMM frames, or two messages in one frame, are counted correctly.

Usage (needs tshark on PATH; run from the repository root):
  scripts/message_stream_tally.py            one line per log that holds an ANC Set, then TOTAL
  scripts/message_stream_tally.py -v         one line per log

Counted per log: get (08 11 00 00, phone), set (08 12, phone), ack (ff 01 .. 08 12), nakRR (ff 02 .. RR 08 12, RR = reason),
notify_00 / notify_XX (08 13, by its Settable byte). CAP-002's first 2,663 frames are CAP-001's (DECISIONS.md ADR-018 Update)
and are skipped. CAP-049's combined log repeats its .log.last: the TOTAL line counts that capture twice (71 Sets / 60 ACKs
printed = 70 / 59 distinct).
"""
import subprocess,sys,collections,glob,os
def msgs(log):
    out=subprocess.run(['tshark','-r',log,'-Y','btrfcomm.len>0 && (btrfcomm.dlci==4 || btrfcomm.dlci==5)','-T','fields','-e','frame.number','-e','frame.p2p_dir','-e','bthci_acl.chandle','-e','btrfcomm.dlci','-e','data.data'],capture_output=True,text=True)
    if out.returncode!=0: print('TSHARK ERROR',log,out.stderr,file=sys.stderr); return
    buf=collections.defaultdict(bytearray)
    for line in out.stdout.splitlines():
        p=line.split('\t')
        if len(p)<5 or not p[4]: continue
        fn=int(p[0]); key=(p[1],p[2],p[3]); b=buf[key]; b+=bytes.fromhex(p[4].replace(':',''))
        while len(b)>=4:
            ln=(b[2]<<8)|b[3]
            if ln>1024: b.clear(); break
            if len(b)<4+ln: break
            yield fn,p[1],p[3],bytes(b[:4+ln]); del b[:4+ln]
tot=collections.Counter()
for log in sorted(glob.glob('captures/CAP-*/CAP-*-btsnoop_hci*.log*')):
    name=os.path.basename(log); c=collections.Counter(); first={}
    for fn,d,dlci,m in msgs(log):
        if name.startswith('CAP-002') and fn<=2663: continue  # CAP-001's frames (ADR-018 Update)
        k=None
        if m[:2]==b'\x08\x12' and d=='0': k='set'
        elif m[:2]==b'\xff\x01' and m[4:6]==b'\x08\x12': k='ack'
        elif m[:2]==b'\xff\x02' and m[5:7]==b'\x08\x12': k='nak%02x'%m[4]
        elif m[:2]==b'\x08\x13': k='notify_'+('00' if m[6]==0 else '%02x'%m[6])
        elif m[:4]==b'\x08\x11\x00\x00' and d=='0': k='get'
        if k: c[k]+=1; tot[k]+=1; first.setdefault(k,fn)
    if c.get('set') or '-v' in sys.argv: print(name, dict(c))
print('TOTAL',dict(tot))
