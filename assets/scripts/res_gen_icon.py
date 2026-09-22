from PIL import Image
from pathlib import Path

# Resolutions to generate
SIZES = [16, 20, 24, 32, 40, 48, 64, 128, 256]

# Folder containing this script
folder = Path(__file__).parent

# Find PNG files in the same folder
png_files = list(folder.glob("*.png"))

if not png_files:
    print("ERROR: No PNG file found in the script's folder.")
    input("Press Enter to exit...")
    raise SystemExit

if len(png_files) > 1:
    print("Multiple PNG files found:")
    for i, file in enumerate(png_files, 1):
        print(f"  {i}. {file.name}")

    choice = int(input("Enter the number of the image to use: "))
    source = png_files[choice - 1]
else:
    source = png_files[0]

print(f"Source image: {source.name}")

# Open image
image = Image.open(source).convert("RGBA")

# Output folder
output_folder = folder / "icons"
output_folder.mkdir(exist_ok=True)

# Generate each resolution
for size in SIZES:
    resized = image.resize(
        (size, size),
        Image.Resampling.LANCZOS
    )

    output = output_folder / f"{source.stem}_{size}.png"
    resized.save(output, optimize=True)

    print(f"Generated: {output.name}")

print()
print(f"Done! Icons are in: {output_folder}")
input("Press Enter to exit...")