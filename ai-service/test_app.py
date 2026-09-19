import io

import pytest

from app import app


@pytest.fixture
def client():
    app.config.update(TESTING=True)
    with app.test_client() as client:
        yield client


def test_health_endpoint(client):
    response = client.get("/health")
    assert response.status_code == 200
    assert response.get_json()["status"] == "UP"


def test_predict_requires_image(client):
    response = client.post("/predict")
    assert response.status_code == 400
    assert "No image file provided" in response.get_json()["error"]


def test_predict_rejects_empty_filename(client):
    response = client.post(
        "/predict",
        data={"image": (io.BytesIO(b""), "")},
        content_type="multipart/form-data",
    )
    assert response.status_code == 400
    assert response.get_json()["error"] == "Empty filename provided."


def test_predict_rejects_oversized_upload(client):
    response = client.post(
        "/predict",
        data={"image": (io.BytesIO(b"x" * (10 * 1024 * 1024 + 1)), "large.jpg")},
        content_type="multipart/form-data",
    )
    assert response.status_code == 413
