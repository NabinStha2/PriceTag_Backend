#!/usr/bin/env python3
"""Ensure every documented OpenAPI operation has a matching Postman request."""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path
from urllib.parse import urlsplit


POSTMAN_ROOT = Path("postman/collections/PriceTag Backend")
METHODS = {"get", "post", "put", "patch", "delete", "options", "head", "trace"}
IGNORED_METHODS = {"options", "head"}
URL_PATTERN = re.compile(r"^\s*url:\s*['\"]?(.*?)['\"]?\s*$")
METHOD_PATTERN = re.compile(r"^\s*method:\s*([A-Za-z]+)\s*$")


def normalize_path(path: str) -> str:
    path = path.split("?", 1)[0].strip().rstrip("/")
    if not path:
        return "/"
    # Match Postman's double braces before OpenAPI's single braces so no
    # trailing brace remains ({{id}} must normalize to :param, not :param}).
    return re.sub(r"\{\{[^}/]+\}\}|\{[^}/]+\}|:[^/]+", ":param", path)


def read_postman_operations() -> set[tuple[str, str]]:
    operations: set[tuple[str, str]] = set()

    for request_file in POSTMAN_ROOT.rglob("*.request.yaml"):
        method = None
        url = None
        for line in request_file.read_text(encoding="utf-8").splitlines():
            method_match = METHOD_PATTERN.match(line)
            if method_match:
                method = method_match.group(1).lower()
                continue

            url_match = URL_PATTERN.match(line)
            if url_match:
                url = url_match.group(1).strip()

        if method not in METHODS or not url:
            print(
                f"Invalid request definition: {request_file} "
                "(expected method and url)",
                file=sys.stderr,
            )
            raise SystemExit(1)

        if url.startswith("{{baseUrl}}"):
            path = url.removeprefix("{{baseUrl}}")
        else:
            path = urlsplit(url).path

        operations.add((method, normalize_path(path)))

    return operations


def read_openapi_operations(spec_file: Path) -> set[tuple[str, str]]:
    try:
        document = json.loads(spec_file.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        print(f"Unable to read OpenAPI document {spec_file}: {error}", file=sys.stderr)
        raise SystemExit(1) from error

    operations: set[tuple[str, str]] = set()
    for path, path_item in document.get("paths", {}).items():
        if not isinstance(path_item, dict):
            continue
        for method in path_item:
            if method.lower() in METHODS and method.lower() not in IGNORED_METHODS:
                operations.add((method.lower(), normalize_path(path)))

    return operations


def main() -> int:
    if len(sys.argv) != 2:
        print("Usage: validate_postman_collection.py <openapi.json>", file=sys.stderr)
        return 2

    spec_file = Path(sys.argv[1])
    if not POSTMAN_ROOT.is_dir():
        print(f"Postman collection directory does not exist: {POSTMAN_ROOT}", file=sys.stderr)
        return 1

    openapi_operations = read_openapi_operations(spec_file)
    postman_operations = read_postman_operations()
    missing = sorted(openapi_operations - postman_operations)

    if missing:
        print("Missing Postman requests:")
        for method, path in missing:
            print(f"  {method.upper():6} {path}")
        return 1

    print(
        f"Postman validation passed: {len(openapi_operations)} OpenAPI operations "
        f"have matching requests."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
