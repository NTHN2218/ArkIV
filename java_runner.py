import os
import sys
import re
import subprocess
from pathlib import Path


# ============================================================
# Configuration
# ============================================================

ROOT = Path(__file__).resolve().parent
OUT_DIR = ROOT / "out"
LIB_DIR = ROOT / "lib"


# ============================================================
# Utility
# ============================================================

def run_command(command):
    return subprocess.run(
        command,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        encoding="utf-8",
        errors="replace"
    )


def header(text):
    print()
    print("=" * 60)
    print(text)
    print("=" * 60)


# ============================================================
# Check Java
# ============================================================

def check_java():

    java = run_command(["java", "-version"])
    javap = run_command(["javap", "-version"])

    if java.returncode != 0:
        print("[ERROR] Java was not found.")
        return False

    if javap.returncode != 0:
        print("[ERROR] javap was not found.")
        return False

    print("[OK] Java and javap are available.")

    return True


# ============================================================
# Determine search directory
# ============================================================

def get_search_root():

    if OUT_DIR.is_dir():

        print("[INFO] Found 'out' directory.")
        print("[INFO] Searching only inside:")
        print(f"       {OUT_DIR}")

        return OUT_DIR

    print("[INFO] No 'out' directory found.")
    print("[INFO] Searching entire project.")

    return ROOT


# ============================================================
# Find .class files
# ============================================================

def find_class_files(root):

    class_files = []

    for path in root.rglob("*.class"):

        # Never search inside .git
        if ".git" in path.parts:
            continue

        class_files.append(path)

    return class_files


# ============================================================
# Inspect class for main()
# ============================================================

def inspect_class(class_file):

    result = run_command([
        "javap",
        "-p",
        str(class_file)
    ])

    if result.returncode != 0:
        return None

    output = result.stdout

    # --------------------------------------------------------
    # Get actual class name
    # --------------------------------------------------------

    class_name = None

    pattern = re.compile(
        r"^\s*(?:public\s+|protected\s+|private\s+)?"
        r"(?:final\s+|abstract\s+|static\s+)*"
        r"(?:class|interface|enum|record)\s+"
        r"([A-Za-z_$][A-Za-z0-9_$]*(?:\.[A-Za-z_$][A-Za-z0-9_$]*)*)"
    )

    for line in output.splitlines():

        match = pattern.match(line)

        if match:
            class_name = match.group(1)
            break

    if class_name is None:
        return None

    # --------------------------------------------------------
    # Look for:
    #
    # public static void main(java.lang.String[])
    #
    # --------------------------------------------------------

    for line in output.splitlines():

        line = line.strip()

        if " main(" not in line:
            continue

        if "static" not in line:
            continue

        if "void" not in line:
            continue

        if "java.lang.String[]" not in line:
            continue

        return class_name

    return None


# ============================================================
# Determine classpath root
# ============================================================

def get_classpath_root(class_file, class_name):

    package_depth = len(class_name.split(".")) - 1

    root = class_file.parent

    for _ in range(package_depth):
        root = root.parent

    return root


# ============================================================
# Find FIRST main()
# ============================================================

def find_main_class(search_root):

    print()
    print("[INFO] Searching for main() entry point...")
    print()

    class_files = find_class_files(search_root)

    print(f"[INFO] Found {len(class_files)} compiled class files.")
    print()

    for class_file in class_files:

        # ----------------------------------------------------
        # Show what is being checked
        # ----------------------------------------------------

        print(f"[CHECK] {class_file}")

        class_name = inspect_class(class_file)

        if class_name is None:
            continue

        classpath_root = get_classpath_root(
            class_file,
            class_name
        )

        print()
        print("[FOUND] main() entry point")
        print(f"        Class:     {class_name}")
        print(f"        File:      {class_file}")
        print(f"        Classpath: {classpath_root}")
        print()

        # ----------------------------------------------------
        # IMPORTANT:
        # Stop immediately.
        # ----------------------------------------------------

        return (
            class_name,
            class_file,
            classpath_root
        )

    return None


# ============================================================
# Build classpath
# ============================================================

def build_classpath(classpath_root):

    entries = [
        str(classpath_root)
    ]

    print("[INFO] Classpath root:")
    print(f"       {classpath_root}")

    # --------------------------------------------------------
    # Look for lib/
    # --------------------------------------------------------

    if LIB_DIR.is_dir():

        jars = sorted(LIB_DIR.glob("*.jar"))

        if jars:

            print()
            print("[INFO] Found 'lib' directory.")
            print("[INFO] Adding JAR files:")
            print()

            for jar in jars:

                print(f"       {jar.name}")

                entries.append(
                    str(jar)
                )

        else:

            print()
            print("[INFO] 'lib' directory contains no JAR files.")

    else:

        print()
        print("[INFO] No 'lib' directory found.")

    return os.pathsep.join(entries)


# ============================================================
# Run Java
# ============================================================

def run_java(class_name, classpath):

    header("RUNNING")

    print(f"Class:     {class_name}")
    print(f"Classpath: {classpath}")
    print()
    print("[INFO] Starting Java...")
    print()

    command = [
        "java",
        "-cp",
        classpath,
        class_name
    ]

    # Pass any arguments given to run.bat
    command.extend(sys.argv[1:])

    result = subprocess.run(command)

    return result.returncode


# ============================================================
# Main
# ============================================================

def main():

    header("UNIVERSAL JAVA RUNNER")

    print(f"Project: {ROOT}")

    # --------------------------------------------------------
    # Check Java
    # --------------------------------------------------------

    if not check_java():
        return 1

    # --------------------------------------------------------
    # Determine search location
    # --------------------------------------------------------

    search_root = get_search_root()

    # --------------------------------------------------------
    # Find FIRST main()
    # --------------------------------------------------------

    main_class = find_main_class(search_root)

    # --------------------------------------------------------
    # No main found
    # --------------------------------------------------------

    if main_class is None:

        print()
        print("=" * 60)
        print("[ERROR] No main(String[]) entry point was found.")
        print("=" * 60)

        return 1

    # --------------------------------------------------------
    # We found one
    # --------------------------------------------------------

    class_name, class_file, classpath_root = main_class

    print()
    print("=" * 60)
    print("[INFO] Selected entry point")
    print("=" * 60)

    print(f"Class:      {class_name}")
    print(f"Class file: {class_file}")
    print(f"Classpath:  {classpath_root}")

    # --------------------------------------------------------
    # Build classpath
    # --------------------------------------------------------

    classpath = build_classpath(
        classpath_root
    )

    # --------------------------------------------------------
    # Actually RUN Java
    # --------------------------------------------------------

    return run_java(
        class_name,
        classpath
    )


# ============================================================
# Program entry
# ============================================================

if __name__ == "__main__":

    try:

        exit_code = main()

        sys.exit(exit_code)

    except KeyboardInterrupt:

        print()
        print("[INFO] Interrupted by user.")

        sys.exit(130)

    except Exception as error:

        print()
        print("=" * 60)
        print("[ERROR] Unexpected error")
        print("=" * 60)
        print()
        print(error)

        sys.exit(1)