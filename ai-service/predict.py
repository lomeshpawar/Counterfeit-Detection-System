"""
CounterCheck AI service - image-feature heuristic for product authenticity demos.

This prototype combines generic ImageNet MobileNetV2 outputs with hand-designed
image statistics. It is not a counterfeit-specific trained or validated model,
and its returned confidence value is a heuristic score, not calibrated probability.
"""

import io
from PIL import Image, ImageStat, ImageFilter

# Optional PyTorch import
try:
    import torch
    import torchvision.transforms as transforms
    from torchvision.models import mobilenet_v2, MobileNet_V2_Weights
    HAS_TORCH = True
except ImportError:
    HAS_TORCH = False


class ProductClassifier:
    """Prototype image-feature heuristic; not a validated authenticity classifier."""

    def __init__(self):
        self.model = None
        if HAS_TORCH:
            try:
                self.weights = MobileNet_V2_Weights.DEFAULT
                self.model = mobilenet_v2(weights=self.weights)
                self.model.eval()
                self.transform = transforms.Compose([
                    transforms.Resize((224, 224)),
                    transforms.ToTensor(),
                    transforms.Normalize(
                        mean=[0.485, 0.456, 0.406],
                        std=[0.229, 0.224, 0.225]
                    )
                ])
                print("[AI] Pretrained ImageNet MobileNetV2 feature signal enabled.")
            except Exception as e:
                print(f"[AI] PyTorch weights fallback: {e}")
        else:
            print("[AI] Running in Pillow texture and edge heuristic mode.")

    def analyze_image(self, image_bytes):
        image = Image.open(io.BytesIO(image_bytes)).convert("RGB")

        # Image quality, detail, and texture analytics
        gray_img = image.convert("L")
        edges = gray_img.filter(ImageFilter.FIND_EDGES)
        edge_stat = ImageStat.Stat(edges)
        edge_std = edge_stat.stddev[0]

        color_stat = ImageStat.Stat(image)
        color_std_avg = sum(color_stat.stddev) / 3.0

        gray_stat = ImageStat.Stat(gray_img)
        contrast = gray_stat.stddev[0]

        # Generic ImageNet top-class probability is only a weak feature signal.
        # It does not directly estimate product authenticity.
        mobilenet_score = 0.5
        if self.model is not None and HAS_TORCH:
            try:
                with torch.no_grad():
                    input_tensor = self.transform(image).unsqueeze(0)
                    output = self.model(input_tensor)
                    probs = torch.softmax(output, dim=1)
                    top_prob, _ = torch.max(probs, dim=1)
                    mobilenet_score = top_prob.item()
            except Exception:
                mobilenet_score = 0.5

        # Hand-designed heuristic weights; these are not learned from a
        # labelled genuine/counterfeit training dataset.
        sharpness_norm = min(max((edge_std - 8.0) / 30.0, 0.0), 1.0)
        contrast_norm = min(max((contrast - 15.0) / 45.0, 0.0), 1.0)
        color_norm = min(max((color_std_avg - 12.0) / 40.0, 0.0), 1.0)

        combined_score = (
            sharpness_norm * 0.40
            + contrast_norm * 0.25
            + color_norm * 0.20
            + mobilenet_score * 0.15
        )

        is_genuine = combined_score >= 0.45
        if is_genuine:
            prediction = "Genuine"
            confidence = min(round(80.0 + (combined_score * 18.5), 2), 98.50)
        else:
            prediction = "Counterfeit"
            confidence = min(round(80.0 + ((1.0 - combined_score) * 17.5), 2), 97.50)

        model_name = (
            "MobileNetV2 ImageNet + texture heuristic (unvalidated)"
            if self.model is not None and HAS_TORCH
            else "Computer vision texture heuristic (unvalidated)"
        )

        return {
            "prediction": prediction,
            "confidence": confidence,
            "model_used": model_name
        }


classifier = ProductClassifier()


def predict_image(image_bytes):
    return classifier.analyze_image(image_bytes)
