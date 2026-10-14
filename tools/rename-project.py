#!/usr/bin/env python3
import argparse
import re
import shutil
import sys
from pathlib import Path

OLD_PACKAGE = "com.example.globalarch"
OLD_PACKAGE_PATH = OLD_PACKAGE.replace(".", "/")
OLD_ID = "GlobalArch"
OLD_ROOT_NAME = "GlobalArchitecture"
OLD_APP_NAMES = ("Global Architecture", "通用架构脚手架")

TEXT_SUFFIXES = {".kt", ".kts", ".xml", ".pro", ".toml", ".md", ".properties", ".json", ".yml", ".yaml"}
SKIP_DIRS = {"build", ".gradle", ".kotlin", ".idea", ".git"}
PACKAGE_PATTERN = re.compile(r"^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$")
KOTLIN_KEYWORDS = {
    "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if", "in", "interface",
    "is", "null", "object", "package", "return", "super", "this", "throw", "true", "try", "typealias",
    "typeof", "val", "var", "when", "while",
}


def parse_args():
    parser = argparse.ArgumentParser(
        description="Rename a copied scaffold project: package, application id, app name, class prefix.",
    )
    parser.add_argument("project_dir", type=Path, help="copied single-module or multi-module directory")
    parser.add_argument("--package", required=True, help="new base package, e.g. com.acme.notes")
    parser.add_argument("--name", required=True, help="new app display name, e.g. \"Acme Notes\"")
    parser.add_argument(
        "--id",
        dest="identifier",
        help="PascalCase identifier for rootProject.name, theme and Application class; default derived from --name",
    )
    return parser.parse_args()


def derive_identifier(name: str) -> str:
    parts = re.findall(r"[A-Za-z0-9]+", name)
    return "".join(part[:1].upper() + part[1:] for part in parts)


def validate(package: str, identifier: str):
    if not PACKAGE_PATTERN.match(package):
        sys.exit(f"invalid package '{package}': use lowercase segments like com.acme.notes")
    if any(segment in KOTLIN_KEYWORDS for segment in package.split(".")):
        sys.exit(f"invalid package '{package}': a segment is a Kotlin keyword")
    if not re.match(r"^[A-Z][A-Za-z0-9]*$", identifier):
        sys.exit(f"invalid identifier '{identifier}': must be PascalCase letters and digits")


def iter_files(root: Path):
    for path in root.rglob("*"):
        if any(part in SKIP_DIRS for part in path.relative_to(root).parts):
            continue
        if path.is_file():
            yield path


def rewrite_text(root: Path, package: str, name: str, identifier: str) -> int:
    changed = 0
    for path in iter_files(root):
        if path.suffix not in TEXT_SUFFIXES:
            continue
        original = path.read_text(encoding="utf-8")
        text = original.replace(OLD_PACKAGE, package).replace(OLD_PACKAGE_PATH, package.replace(".", "/"))
        text = text.replace(f"{OLD_ID}Application", f"{identifier}Application")
        text = text.replace(f"Theme.{OLD_ID}", f"Theme.{identifier}")
        text = text.replace(OLD_ROOT_NAME, identifier)
        if path.name == "strings.xml":
            for old in OLD_APP_NAMES:
                text = text.replace(f">{old}<", f">{name}<")
        if text != original:
            path.write_text(text, encoding="utf-8")
            changed += 1
    return changed


def prune_empty_parents(path: Path, stop: Path):
    current = path
    while current != stop and current.exists() and not any(current.iterdir()):
        current.rmdir()
        current = current.parent


def move_package_directories(root: Path, package: str) -> int:
    new_parts = package.split(".")
    old_parts = OLD_PACKAGE.split(".")
    moved = 0
    candidates = sorted(
        (p for p in root.rglob(old_parts[-1]) if p.is_dir()),
        key=lambda p: len(p.parts),
        reverse=True,
    )
    for directory in candidates:
        relative = directory.relative_to(root)
        if any(part in SKIP_DIRS for part in relative.parts):
            continue
        if list(relative.parts[-len(old_parts):]) != old_parts:
            continue
        base = directory
        for _ in old_parts:
            base = base.parent
        target = base.joinpath(*new_parts)
        if target == directory:
            continue
        target.parent.mkdir(parents=True, exist_ok=True)
        if target.exists():
            sys.exit(f"target already exists: {target}")
        shutil.move(str(directory), str(target))
        prune_empty_parents(directory.parent, base)
        moved += 1
    return moved


def rename_schema_directories(root: Path, package: str) -> int:
    renamed = 0
    for directory in sorted((p for p in root.rglob(f"{OLD_PACKAGE}*") if p.is_dir()), key=lambda p: len(p.parts), reverse=True):
        if any(part in SKIP_DIRS for part in directory.relative_to(root).parts):
            continue
        directory.rename(directory.with_name(directory.name.replace(OLD_PACKAGE, package)))
        renamed += 1
    return renamed


def rename_files(root: Path, identifier: str) -> int:
    renamed = 0
    for path in list(iter_files(root)):
        if path.name == f"{OLD_ID}Application.kt":
            path.rename(path.with_name(f"{identifier}Application.kt"))
            renamed += 1
    return renamed


def main():
    args = parse_args()
    root = args.project_dir.resolve()
    identifier = args.identifier or derive_identifier(args.name)
    validate(args.package, identifier)
    if not (root / "settings.gradle.kts").is_file():
        sys.exit(f"not a scaffold project (settings.gradle.kts missing): {root}")
    if not any(OLD_PACKAGE in p.read_text(encoding="utf-8") for p in iter_files(root) if p.suffix == ".kts"):
        sys.exit(f"'{OLD_PACKAGE}' not found: already renamed or not a scaffold project")

    text_files = rewrite_text(root, args.package, args.name, identifier)
    moved = move_package_directories(root, args.package)
    schemas = rename_schema_directories(root, args.package)
    files = rename_files(root, identifier)

    print(f"project      : {root}")
    print(f"package      : {OLD_PACKAGE} -> {args.package}")
    print(f"identifier   : {identifier}")
    print(f"app name     : {args.name}")
    print(f"text files   : {text_files} rewritten")
    print(f"directories  : {moved} package dirs moved, {schemas} schema dirs renamed")
    print(f"files        : {files} renamed")
    print("next         : ./gradlew spotlessApply assembleDebug testDebugUnitTest")


if __name__ == "__main__":
    main()
