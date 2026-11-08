#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
BUILD_TOOLS="${BUILD_TOOLS:-}"
if [[ -z "$BUILD_TOOLS" && -n "$SDK" && -d "$SDK/build-tools" ]]; then
    BUILD_TOOLS="$(find "$SDK/build-tools" -mindepth 1 -maxdepth 1 -type d | sort | tail -n 1)"
fi

resolve_tool() {
    local name="$1" override="${2:-}"
    if [[ -n "$override" ]]; then
        printf '%s\n' "$override"
        return
    fi
    if command -v "$name" >/dev/null 2>&1; then
        command -v "$name"
        return
    fi
    if [[ -n "$BUILD_TOOLS" && -x "$BUILD_TOOLS/$name" ]]; then
        printf '%s\n' "$BUILD_TOOLS/$name"
        return
    fi
    echo "error: $name not found. Install Android SDK Build-Tools or add it to PATH." >&2
    exit 1
}

AAPT2="$(resolve_tool aapt2 "${AAPT2:-}")"
D8="$(resolve_tool d8 "${D8:-}")"
ZIPALIGN="$(resolve_tool zipalign "${ZIPALIGN:-}")"
APKSIGNER="$(resolve_tool apksigner "${APKSIGNER:-}")"
JAVAC="$(resolve_tool javac "${JAVAC:-}")"
KEYTOOL="$(resolve_tool keytool "${KEYTOOL:-}")"
PYTHON="$(resolve_tool python3 "${PYTHON:-}")"

if [[ -z "${ANDROID_JAR:-}" && -n "$SDK" && -d "$SDK/platforms" ]]; then
    ANDROID_JAR="$(find "$SDK/platforms" -mindepth 2 -maxdepth 2 -name android.jar \
        | sort | tail -n 1)"
fi
if [[ -z "${ANDROID_JAR:-}" || ! -f "$ANDROID_JAR" ]]; then
    echo "error: ANDROID_JAR is not set." >&2
    echo "Set it to an Android platform android.jar (API 36 or newer), for example:" >&2
    echo "  export ANDROID_JAR=\"\$ANDROID_HOME/platforms/android-36/android.jar\"" >&2
    exit 1
fi

KEYSTORE="${KEYSTORE:-$ROOT/linkding.keystore}"
KEY_ALIAS="${KEY_ALIAS:-linkding}"
STORE_PASS="${STORE_PASS:-android}"
KEY_PASS="${KEY_PASS:-$STORE_PASS}"
VERSION_CODE="${VERSION_CODE:-102}"
VERSION_NAME="${VERSION_NAME:-1.0.2}"
MIN_SDK="${MIN_SDK:-26}"
TARGET_SDK="${TARGET_SDK:-36}"

if [[ ! -f "$KEYSTORE" ]]; then
    echo "Generating a development signing key at $KEYSTORE"
    "$KEYTOOL" -genkeypair \
        -keystore "$KEYSTORE" \
        -alias "$KEY_ALIAS" \
        -storepass "$STORE_PASS" \
        -keypass "$KEY_PASS" \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -dname "CN=LinkDing, OU=Development, O=Arif Nurrohim, L=Unknown, ST=Unknown, C=ID"
fi

rm -rf build
mkdir -p build/classes build/dex

"$AAPT2" compile --dir src/main/res -o build/res.zip

"$AAPT2" link \
    -o build/base.apk \
    -I "$ANDROID_JAR" \
    --manifest src/main/AndroidManifest.xml \
    -R build/res.zip \
    --auto-add-overlay \
    -A src/main/assets \
    --min-sdk-version "$MIN_SDK" \
    --target-sdk-version "$TARGET_SDK" \
    --version-code "$VERSION_CODE" \
    --version-name "$VERSION_NAME" \
    --replace-version

find src/main/java -name '*.java' -print0 \
    | xargs -0 "$JAVAC" -source 8 -target 8 -Xlint:-options \
        -bootclasspath "$ANDROID_JAR" \
        -d build/classes

find build/classes -name '*.class' -print0 \
    | xargs -0 "$D8" --lib "$ANDROID_JAR" --min-api "$MIN_SDK" \
        --output build/dex

cp build/base.apk build/unsigned.apk

"$PYTHON" - build/unsigned.apk build/dex/classes.dex <<'PY'
import sys
import zipfile

apk, dex = sys.argv[1], sys.argv[2]
with zipfile.ZipFile(apk, "a", zipfile.ZIP_STORED) as z:
    z.write(dex, "classes.dex")
PY

"$ZIPALIGN" -f 4 build/unsigned.apk build/aligned.apk

"$APKSIGNER" sign \
    --ks "$KEYSTORE" \
    --ks-key-alias "$KEY_ALIAS" \
    --ks-pass "pass:$STORE_PASS" \
    --key-pass "pass:$KEY_PASS" \
    --out LinkDing.apk \
    build/aligned.apk

"$APKSIGNER" verify --verbose LinkDing.apk

ls -l LinkDing.apk
