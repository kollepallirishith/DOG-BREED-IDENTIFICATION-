import torch
import torchvision.transforms as transforms
from torchvision.transforms.functional import InterpolationMode
from PIL import Image
from os.path import dirname, join
import numpy as np

# Load the saved model from file
model = torch.load(join(dirname(__file__),"FullModel/inceptionv3_model_full.pth"), map_location=torch.device('cpu'))
labels = ['affenpinscher',
          'afghan_hound',
          'african_hunting_dog',
          'airedale',
          'american_staffordshire_terrier',
          'appenzeller',
          'australian_terrier',
          'basenji',
          'basset',
          'beagle',
          'bedlington_terrier',
          'bernese_mountain_dog',
          'black-and-tan_coonhound',
          'blenheim_spaniel',
          'bloodhound',
          'bluetick',
          'border_collie',
          'border_terrier',
          'borzoi',
          'boston_bull',
          'bouvier_des_flandres',
          'boxer',
          'brabancon_griffon',
          'briard',
          'brittany_spaniel',
          'bull_mastiff',
          'cairn',
          'cardigan',
          'chesapeake_bay_retriever',
          'chihuahua',
          'chow',
          'clumber',
          'cocker_spaniel',
          'collie',
          'curly-coated_retriever',
          'dandie_dinmont',
          'dhole',
          'dingo',
          'doberman',
          'english_foxhound',
          'english_setter',
          'english_springer',
          'entlebucher',
          'eskimo_dog',
          'flat-coated_retriever',
          'french_bulldog',
          'german_shepherd',
          'german_short-haired_pointer',
          'giant_schnauzer',
          'golden_retriever',
          'gordon_setter',
          'great_dane',
          'great_pyrenees',
          'greater_swiss_mountain_dog',
          'groenendael',
          'ibizan_hound',
          'irish_setter',
          'irish_terrier',
          'irish_water_spaniel',
          'irish_wolfhound',
          'italian_greyhound',
          'japanese_spaniel',
          'keeshond',
          'kelpie',
          'kerry_blue_terrier',
          'komondor',
          'kuvasz',
          'labrador_retriever',
          'lakeland_terrier',
          'leonberg',
          'lhasa',
          'malamute',
          'malinois',
          'maltese_dog',
          'mexican_hairless',
          'miniature_pinscher',
          'miniature_poodle',
          'miniature_schnauzer',
          'newfoundland',
          'norfolk_terrier',
          'norwegian_elkhound',
          'norwich_terrier',
          'old_english_sheepdog',
          'otterhound',
          'papillon',
          'pekinese',
          'pembroke',
          'pomeranian',
          'pug',
          'redbone',
          'rhodesian_ridgeback',
          'rottweiler',
          'saint_bernard',
          'saluki',
          'samoyed',
          'schipperke',
          'scotch_terrier',
          'scottish_deerhound',
          'sealyham_terrier',
          'shetland_sheepdog',
          'shih-tzu',
          'siberian_husky',
          'silky_terrier',
          'soft-coated_wheaten_terrier',
          'staffordshire_bullterrier',
          'standard_poodle',
          'standard_schnauzer',
          'sussex_spaniel',
          'tibetan_mastiff',
          'tibetan_terrier',
          'toy_poodle',
          'toy_terrier',
          'vizsla',
          'walker_hound',
          'weimaraner',
          'welsh_springer_spaniel',
          'west_highland_white_terrier',
          'whippet',
          'wire-haired_fox_terrier',
          'yorkshire_terrier']
img_tensor = None

def load_image(raw_data_bytes, width, height):
    global img_tensor
    img = Image.frombytes("RGB", (width, height), bytes(raw_data_bytes))
#     img = Image.fromarray(raw_data_bytes)

    transformed = transforms.Compose([
            transforms.Resize(size = 224, interpolation=InterpolationMode.BILINEAR),
            transforms.CenterCrop(size = 224),
            transforms.ToTensor(),
            transforms.Normalize([0.485, 0.456, 0.406],
                                 [0.229, 0.224, 0.225])
        ])

    # Apply the transformation
    img_tensor = transformed(img).unsqueeze(0)

def predict():
    with torch.no_grad():
        output = model(img_tensor)

    softmax_output = output[0].tolist()
    denormalized_output = np.exp(softmax_output) / np.sum(np.exp(softmax_output))

    top_5_values = sorted(denormalized_output, reverse=True)[:5]

    top_5_indices = [i for i, x in enumerate(denormalized_output) if x in top_5_values]

    top_5_labels = []
    label_confidence = {}
    for index in top_5_indices:
      top_5_labels.append(labels[index])

    for label, confidence in zip(top_5_labels, top_5_values):
      label_confidence[label] = confidence

    return label_confidence



