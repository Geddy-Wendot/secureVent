import sys
import math
from PIL import Image

def calculate_entropy(image_path):
    try:
        img = Image.open(image_path)
        # Convert to Grayscale to simplify the matrix (Vector analysis)
        img = img.convert('L') 
        
        # Get the histogram (frequency of each pixel value 0-255)
        histogram = img.histogram()
        image_size = img.size[0] * img.size[1]
        
        entropy = 0
        
        # Shannon Entropy Formula: H = -sum(p * log2(p))
        for count in histogram:
            if count > 0:
                p = count / image_size  # Probability of this color occurring
                entropy -= p * math.log2(p)
                
        return entropy
    except Exception as e:
        return 0

if __name__ == "__main__":
    # Get image path from Java arguments
    if len(sys.argv) > 1:
        img_path = sys.argv[1]
        score = calculate_entropy(img_path)
        
        # Threshold: 5.0 is a good baseline for "complex enough"
        if score > 5.0:
            print(f"SAFE|{score:.2f}")
        else:
            print(f"UNSAFE|{score:.2f}")
    else:
        print("ERROR|No path provided")