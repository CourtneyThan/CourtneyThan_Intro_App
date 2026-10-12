#!/usr/bin/env bash
# Downloads the two fonts the design uses (both free, SIL Open Font License)
# into core/designsystem/src/main/assets/fonts. Run once after cloning:
#   ./scripts/download_fonts.sh
# Without them the app still builds and runs, using the system font.
set -euo pipefail
cd "$(dirname "$0")/.."
DEST=core/designsystem/src/main/assets/fonts
mkdir -p "$DEST"
BASE=https://raw.githubusercontent.com/google/fonts/main/ofl
curl -fL "$BASE/fredoka/Fredoka%5Bwdth,wght%5D.ttf" -o "$DEST/fredoka.ttf"
curl -fL "$BASE/nunito/Nunito%5Bwght%5D.ttf" -o "$DEST/nunito.ttf"
curl -fL "$BASE/nunito/OFL.txt" -o "$DEST/OFL.txt"
ls -lh "$DEST"
echo "Fonts ready. Rebuild the app to see Fredoka + Nunito."
