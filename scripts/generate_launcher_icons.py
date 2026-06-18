"""Generate Android launcher icons from the Mnemosyne logo."""

from pathlib import Path

from PIL import Image

SOURCE = Path(
    r"C:\Users\gtnoo\.cursor\projects\c-Users-gtnoo-Documents-Programs-Linear-Alg-Mnemosyne\assets"
    r"\c__Users_gtnoo_AppData_Roaming_Cursor_User_workspaceStorage_9663b7fd94fbba94313a0fb38f8aee17_images"
    r"_LOGO-80d67b82-49df-498b-9622-d5e7d472d37e.png"
)
RES_DIR = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res"

LEGACY_SIZES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

ADAPTIVE_SIZES = {
    "mipmap-mdpi": 108,
    "mipmap-hdpi": 162,
    "mipmap-xhdpi": 216,
    "mipmap-xxhdpi": 324,
    "mipmap-xxxhdpi": 432,
}

ADAPTIVE_ICON_XML = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@mipmap/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
</adaptive-icon>
"""


def resize_logo(source: Image.Image, size: int) -> Image.Image:
    return source.resize((size, size), Image.Resampling.LANCZOS)


def make_foreground(size: int) -> Image.Image:
    """Transparent foreground; logo lives on the background layer."""
    return Image.new("RGBA", (size, size), (0, 0, 0, 0))


def main() -> None:
    source = Image.open(SOURCE).convert("RGBA")

    for folder, size in LEGACY_SIZES.items():
        out_dir = RES_DIR / folder
        out_dir.mkdir(parents=True, exist_ok=True)
        icon = resize_logo(source, size)
        icon.save(out_dir / "ic_launcher.png", optimize=True)
        icon.save(out_dir / "ic_launcher_round.png", optimize=True)

    for folder, size in ADAPTIVE_SIZES.items():
        out_dir = RES_DIR / folder
        out_dir.mkdir(parents=True, exist_ok=True)
        resize_logo(source, size).save(out_dir / "ic_launcher_background.png", optimize=True)
        make_foreground(size).save(out_dir / "ic_launcher_foreground.png", optimize=True)

    adaptive_dir = RES_DIR / "mipmap-anydpi-v26"
    adaptive_dir.mkdir(parents=True, exist_ok=True)
    (adaptive_dir / "ic_launcher.xml").write_text(ADAPTIVE_ICON_XML, encoding="utf-8")
    (adaptive_dir / "ic_launcher_round.xml").write_text(ADAPTIVE_ICON_XML, encoding="utf-8")

    print("Launcher icons generated.")


if __name__ == "__main__":
    main()
