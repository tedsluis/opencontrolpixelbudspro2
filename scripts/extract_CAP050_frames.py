import subprocess
import os

video_path = "captures/CAP-050-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AG/CAP-050-recording.mp4"
output_dir = "/home/tedsluis/.gemini/tmp/opencontrolpixelbudspro2/scratch"

os.makedirs(output_dir, exist_ok=True)

# Extract frames every 1 second
for sec in range(0, 749):
    out_path = f"{output_dir}/frame_{sec:03d}.jpg"
    if os.path.exists(out_path):
        # Only extract if it doesn't already exist or if we want to overwrite
        continue
    cmd = [
        "ffmpeg", "-y",
        "-ss", str(sec),
        "-i", video_path,
        "-frames:v", "1",
        "-vf", "scale=640:-1",
        "-q:v", "5",
        out_path
    ]
    subprocess.run(cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

print("1-second extraction complete!")
