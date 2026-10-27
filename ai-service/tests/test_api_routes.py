import io
import sys
from pathlib import Path
from unittest.mock import patch

import pytest

# Ensure imports work when pytest is invoked from the ai-service directory.
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import app as app_module  # noqa: E402


@pytest.fixture()
def client():
    app_module.app.config.update(TESTING=True)
    with app_module.app.test_client() as test_client:
        yield test_client


def test_health_returns_service_status(client):
    response = client.get("/health")

    assert response.status_code == 200
    assert response.get_json()["status"] == "UP"


def test_predict_rejects_missing_upload(client):
    response = client.post("/predict", data={})

    assert response.status_code == 400
    assert "No image file provided" in response.get_json()["error"]


def test_predict_rejects_empty_filename(client):
    response = client.post(
        "/predict",
        data={"image": (io.BytesIO(b""), "")},
        content_type="multipart/form-data",
    )

    assert response.status_code == 400
    assert "Empty filename" in response.get_json()["error"]


def test_predict_returns_prediction_for_valid_upload(client):
    expected = {"label": "genuine", "confidence": 0.9}
    with patch.object(app_module, "predict_image", return_value=expected) as predict_mock:
        response = client.post(
            "/predict",
            data={"image": (io.BytesIO(b"image-bytes"), "shoe.jpg")},
            content_type="multipart/form-data",
        )

    assert response.status_code == 200
    assert response.get_json() == expected
    predict_mock.assert_called_once_with(b"image-bytes")


def test_predict_returns_400_for_invalid_image(client):
    with patch.object(app_module, "predict_image", side_effect=ValueError("invalid image")):
        response = client.post(
            "/predict",
            data={"image": (io.BytesIO(b"not-an-image"), "shoe.jpg")},
            content_type="multipart/form-data",
        )

    assert response.status_code == 400
    assert "valid image" in response.get_json()["error"]


def test_predict_returns_500_for_unexpected_prediction_error(client):
    with patch.object(app_module, "predict_image", side_effect=RuntimeError("internal detail")):
        response = client.post(
            "/predict",
            data={"image": (io.BytesIO(b"image-bytes"), "shoe.jpg")},
            content_type="multipart/form-data",
        )

    assert response.status_code == 500
    assert response.get_json()["error"] == "Prediction service failed to process the image."
    assert "internal detail" not in response.get_data(as_text=True)


def test_cors_allows_configured_local_origin(client):
    origin = "http://localhost:5173"

    response = client.get("/health", headers={"Origin": origin})

    assert response.status_code == 200
    assert response.headers.get("Access-Control-Allow-Origin") == origin


def test_cors_does_not_allow_unconfigured_origin(client):
    response = client.get(
        "/health",
        headers={"Origin": "https://untrusted.example"},
    )

    assert response.status_code == 200
    assert "Access-Control-Allow-Origin" not in response.headers


def test_cors_preflight_allows_configured_local_origin(client):
    origin = "http://localhost:5173"

    response = client.options(
        "/predict",
        headers={
            "Origin": origin,
            "Access-Control-Request-Method": "POST",
            "Access-Control-Request-Headers": "Content-Type",
        },
    )

    assert response.status_code in (200, 204)
    assert response.headers.get("Access-Control-Allow-Origin") == origin
    assert "POST" in response.headers.get("Access-Control-Allow-Methods", "")


def test_cors_preflight_does_not_allow_unconfigured_origin(client):
    response = client.options(
        "/predict",
        headers={
            "Origin": "https://untrusted.example",
            "Access-Control-Request-Method": "POST",
            "Access-Control-Request-Headers": "Content-Type",
        },
    )

    assert response.status_code in (200, 204)
    assert "Access-Control-Allow-Origin" not in response.headers
}
