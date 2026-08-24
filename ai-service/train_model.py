"""
============================================================
CounterCheck AI Service - Model Training Script
============================================================
Model: MobileNetV2 Transfer Learning Fine-Tuning
Description: Trains and fine-tunes MobileNetV2 on a custom dataset 
             of Genuine and Counterfeit product images.
Dataset structure:
    ai-service/dataset/
        ├── genuine/
        └── counterfeit/
Output: Saves trained model weights to ai-service/model/mobilenet_countercheck.pth
============================================================
"""

import os
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader
from torchvision import datasets, transforms
from torchvision.models import mobilenet_v2, MobileNet_V2_Weights


def train():
    # 1. Paths & Configuration
    base_dir = os.path.dirname(os.path.abspath(__file__))
    dataset_dir = os.path.join(base_dir, "dataset")
    model_dir = os.path.join(base_dir, "model")
    os.makedirs(model_dir, exist_ok=True)
    save_path = os.path.join(model_dir, "mobilenet_countercheck.pth")

    batch_size = 16
    epochs = 10
    learning_rate = 0.001
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"[TRAIN] Operating on compute device: {device}")

    # Check if dataset exists
    if not os.path.exists(dataset_dir) or len(os.listdir(dataset_dir)) == 0:
        print("[TRAIN] Notice: Place your training images inside 'dataset/genuine/' and 'dataset/counterfeit/' folders.")
        print("[TRAIN] Pre-trained MobileNetV2 transfer weights are already active for inference.")
        return

    # 2. Data Transformations & Augmentation
    data_transforms = transforms.Compose([
        transforms.Resize((224, 224)),
        transforms.RandomHorizontalFlip(),
        transforms.RandomRotation(15),
        transforms.ColorJitter(brightness=0.2, contrast=0.2),
        transforms.ToTensor(),
        transforms.Normalize(
            mean=[0.485, 0.456, 0.406],
            std=[0.229, 0.224, 0.225]
        )
    ])

    # 3. Load Dataset
    dataset = datasets.ImageFolder(root=dataset_dir, transform=data_transforms)
    dataloader = DataLoader(dataset, batch_size=batch_size, shuffle=True, num_workers=2)
    print(f"[TRAIN] Loaded dataset with {len(dataset)} samples across classes: {dataset.classes}")

    # 4. Load Pre-trained MobileNetV2
    weights = MobileNet_V2_Weights.DEFAULT
    model = mobilenet_v2(weights=weights)

    # Freeze base feature extractor layers
    for param in model.parameters():
        param.requires_grad = False

    # Replace classifier head for 2 classes: Genuine (0) & Counterfeit (1)
    in_features = model.classifier[1].in_features
    model.classifier[1] = nn.Sequential(
        nn.Dropout(p=0.2),
        nn.Linear(in_features, 2)
    )

    model = model.to(device)

    # 5. Loss & Optimizer
    criterion = nn.CrossEntropyLoss()
    optimizer = optim.Adam(model.classifier[1].parameters(), lr=learning_rate)

    # 6. Training Loop
    print("[TRAIN] Starting MobileNetV2 transfer learning training loop...")
    for epoch in range(1, epochs + 1):
        model.train()
        running_loss = 0.0
        correct = 0
        total = 0

        for inputs, labels in dataloader:
            inputs, labels = inputs.to(device), labels.to(device)

            optimizer.zero_grad()
            outputs = model(inputs)
            loss = criterion(outputs, labels)
            loss.backward()
            optimizer.step()

            running_loss += loss.item() * inputs.size(0)
            _, preds = torch.max(outputs, 1)
            correct += torch.sum(preds == labels.data)
            total += inputs.size(0)

        epoch_loss = running_loss / total
        epoch_acc = (correct.double() / total) * 100.0
        print(f"[Epoch {epoch}/{epochs}] Loss: {epoch_loss:.4f} | Accuracy: {epoch_acc:.2f}%")

    # 7. Save Trained Model Weights
    torch.save(model.state_dict(), save_path)
    print(f"[TRAIN] Fine-tuned model saved successfully to: {save_path}")


if __name__ == "__main__":
    train()
