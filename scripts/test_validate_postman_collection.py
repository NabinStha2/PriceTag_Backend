"""Regression tests for comparing OpenAPI and Postman request paths."""

import io
import json
import tempfile
import unittest
from contextlib import redirect_stdout
from pathlib import Path
from unittest.mock import patch

import validate_postman_collection as validator


class NormalizePathTest(unittest.TestCase):
    def test_supported_parameter_formats_match(self):
        for parameter in ("{productId}", "{{productId}}", ":productId"):
            with self.subTest(parameter=parameter):
                self.assertEqual(
                    validator.normalize_path(f"/api/v1/product/{parameter}"),
                    "/api/v1/product/:param",
                )

    def test_multiple_parameters_and_query_string(self):
        self.assertEqual(
            validator.normalize_path(
                "/api/v1/product/category/{{categoryId}}/"
                "subcategory/{{subcategoryId}}/add/?page={{page}}"
            ),
            "/api/v1/product/category/:param/subcategory/:param/add",
        )

    def test_root_and_trailing_slashes(self):
        for path in ("", "/", "/?page=1"):
            with self.subTest(path=path):
                self.assertEqual(validator.normalize_path(path), "/")
        self.assertEqual(validator.normalize_path("/api/v1/product/"), "/api/v1/product")


class CollectionValidationTest(unittest.TestCase):
    def setUp(self):
        self.temporary_directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary_directory.cleanup)
        self.directory = Path(self.temporary_directory.name)

    def test_request_url_formats(self):
        for index, url in enumerate((
            "'{{baseUrl}}/api/v1/product/{{id}}?page=1'",
            '"https://example.com/api/v1/product/:id"',
            "https://example.com/api/v1/product/{id}/",
        )):
            (self.directory / f"{index}.request.yaml").write_text(
                f"method: GET\nurl: {url}\n", encoding="utf-8"
            )
        with patch.object(validator, "POSTMAN_ROOT", self.directory):
            self.assertEqual(
                validator.read_postman_operations(),
                {("get", "/api/v1/product/:param")},
            )

    def run_validator(self, paths):
        spec_file = self.directory / "openapi.json"
        spec_file.write_text(json.dumps({"paths": paths}), encoding="utf-8")
        collection = Path(__file__).resolve().parents[1] / validator.POSTMAN_ROOT
        output = io.StringIO()
        with (
            patch.object(validator, "POSTMAN_ROOT", collection),
            patch.object(validator.sys, "argv", ["validator", str(spec_file)]),
            redirect_stdout(output),
        ):
            result = validator.main()
        return result, output.getvalue()

    def test_real_collection_covers_templates_and_added_requests(self):
        result, output = self.run_validator({
            "/": {"get": {}},
            "/api/v1/auth/welcome": {"get": {}},
            "/api/v1/rating-review/{id}": {"get": {}, "delete": {}},
            "/api/v1/rating-review/update/{id}": {"patch": {}},
            "/api/v1/orders/{orderNumber}/payment-intent": {"post": {}},
            "/api/v1/product/category/{categoryId}/subcategory/{subcategoryId}/add": {
                "post": {}
            },
        })
        self.assertEqual(result, 0, output)
        self.assertIn("7 OpenAPI operations", output)

    def test_missing_operation_still_fails(self):
        result, output = self.run_validator({
            "/api/v1/not-in-collection/{id}": {"get": {}}
        })
        self.assertEqual(result, 1)
        self.assertIn("GET    /api/v1/not-in-collection/:param", output)

    def test_wrong_method_is_not_a_match(self):
        result, output = self.run_validator({"/api/v1/auth/welcome": {"delete": {}}})
        self.assertEqual(result, 1)
        self.assertIn("DELETE /api/v1/auth/welcome", output)

    def test_head_options_and_path_metadata_are_ignored(self):
        result, output = self.run_validator({
            "/api/v1/auth/welcome": {
                "get": {}, "head": {}, "options": {}, "parameters": []
            }
        })
        self.assertEqual(result, 0, output)
        self.assertIn("1 OpenAPI operations", output)


if __name__ == "__main__":
    unittest.main()
