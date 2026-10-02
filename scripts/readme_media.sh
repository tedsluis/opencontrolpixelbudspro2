#!/usr/bin/env bash
# Turns a phone screen recording into README-ready media, with ffmpeg only (ai-sessions/0065; asked by the maintainer in chat 2026-10-02).
#
#   scripts/readme_media.sh <recording.mp4> <output-prefix> [gif-start-seconds] [gif-length-seconds]
#
# Writes, next to <output-prefix>:
#   <prefix>.mp4          H.264, no audio, 1280 px high (never upscaled), CRF 28, +faststart, all metadata stripped (-map_metadata -1: no creation time,
#                         no Android version tag). GitHub Docs ("Attaching files", fetched 2026-10-02): "10MB for videos uploaded to a repository owned by a
#                         user or organization on a free GitHub plan" and "we recommend using H.264 for greatest compatibility" — this script fails if the
#                         MP4 is 10 MB or more.
#   <prefix>-preview.gif  a short loop (default: 12 s from the start), 320 px wide, 8 fps — plays inline on github.com and on the docs site.
#   <prefix>-poster.png   one frame (at the GIF's start), 540 px wide.
#
# Nothing is uploaded; the files are committed (or not) by the maintainer. Look at the result before committing it: a recording shows whatever was on
# screen (notifications, names) — this script does not blur anything.
set -euo pipefail

if [[ $# -lt 2 ]]; then
    echo "usage: $0 <recording.mp4> <output-prefix> [gif-start-seconds] [gif-length-seconds]" >&2
    exit 2
fi
in="$1"
out="$2"
gif_start="${3:-0}"
gif_len="${4:-12}"
limit=$((10 * 1000 * 1000)) # GitHub's 10 MB video limit (read as 10^7 bytes, the stricter reading)

command -v ffmpeg >/dev/null || { echo "ffmpeg not found" >&2; exit 1; }
[[ -f "$in" ]] || { echo "no such file: $in" >&2; exit 1; }
mkdir -p "$(dirname "$out")"

ffmpeg -loglevel error -y -i "$in" -map 0:v:0 -an -map_metadata -1 -map_chapters -1 \
    -vf "scale=-2:'min(1280,ih)'" -c:v libx264 -preset slow -crf 28 -pix_fmt yuv420p -movflags +faststart \
    "$out.mp4"

ffmpeg -loglevel error -y -ss "$gif_start" -t "$gif_len" -i "$in" -map_metadata -1 \
    -vf "fps=8,scale=320:-2:flags=lanczos,split[a][b];[a]palettegen=max_colors=128[p];[b][p]paletteuse=dither=bayer" \
    "$out-preview.gif"

ffmpeg -loglevel error -y -ss "$gif_start" -i "$in" -map_metadata -1 -frames:v 1 -vf "scale=540:-2" "$out-poster.png"

size=$(stat -c %s "$out.mp4")
for f in "$out.mp4" "$out-preview.gif" "$out-poster.png"; do
    printf '%10d bytes  %s\n' "$(stat -c %s "$f")" "$f"
done
if ((size >= limit)); then
    echo "FAIL: $out.mp4 is $size bytes, not under GitHub's 10 MB video limit — raise the CRF or lower the height." >&2
    exit 1
fi
echo "OK: under the 10 MB limit; metadata stripped. Look at the files before committing them."
