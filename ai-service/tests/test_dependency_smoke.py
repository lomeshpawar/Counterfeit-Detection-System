from io import BytesIO

import torch
from PIL import Image
from torchvision.models import mobilenet_v2


def test_pillow_decodes_a_small_png():
    buffer = BytesIO()
    Image.new("RGB", (8, 8), color=(12, 34, 56)).save(buffer, format="PNG")
    buffer.seek(0)

    with Image.open(buffer) as decoded:
        decoded.load()
        assert decoded.format == "PNG"
        assert decoded.size == (8, 8)


def test_pinned_torchvision_model_runs_a_cpu_forward_pass():
    model = mobilenet_v2(weights=None).eval()
    sample = torch.zeros((1, 3, 224, 224), dtype=torch.float32)

    with torch.inference_mode():
        output = model(sample)

    assert tuple(output.shape) == (1, 1000)
