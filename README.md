## About frame python

from PIL import Image
import requests
from io import BytesIO
import logging

# Configure logging
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")

def process_frame_and_image(frame_path: str, image_url: str, output_path: str):
    # Load the frame image
    logging.info("Loading the frame image...")
    frame = Image.open(frame_path).convert("RGBA")
    frame_width, frame_height = frame.size
    logging.info(f"Frame dimensions: Width={frame_width}, Height={frame_height}")

    # Fetch and load the image from the URL
    logging.info("Fetching the image from the URL...")
    response = requests.get(image_url)
    if response.status_code != 200:
        logging.error(f"Failed to load image from URL. HTTP Error: {response.status_code}")
        return
    image = Image.open(BytesIO(response.content)).convert("RGBA")
    image_width, image_height = image.size
    logging.info(f"Image dimensions: Width={image_width}, Height={image_height}")

    # Resize the frame by cropping it to match the image dimensions
    while frame_width > image_width or frame_height > image_height:
        # Calculate symmetric cropping bounds
        left = (frame_width - image_width) // 2
        top = (frame_height - image_height) // 2
        right = left + image_width
        bottom = top + image_height

        logging.info(f"Cropping frame: Left={left}, Top={top}, Right={right}, Bottom={bottom}")
        frame = frame.crop((left, top, right, bottom))
        frame_width, frame_height = frame.size
        logging.info(f"New frame dimensions: Width={frame_width}, Height={frame_height}")

    logging.info("Frame resized to match the image dimensions.")

    # Combine the frame and the image
    logging.info("Combining the frame and the image...")
    result = Image.new("RGBA", (image_width, image_height), (255, 255, 255, 0))  # Transparent background
    result.paste(frame, (0, 0), mask=frame)  # Add the frame on top
    result.paste(image, (0, 0), mask=image)  # Place the image below the frame

    # Save the output and display it
    logging.info("Saving the result...")
    result.save(output_path, format="PNG")
    logging.info(f"Result saved successfully: {output_path}")
    logging.info("Displaying the result...")
    result.show()  # Display the output

# Example usage
frame_path = "cerceve3.png"  # Path to the local frame image
image_url = "https://changegl.co.uk/wp-content/uploads/2017/12/600x300.png"  # URL of the image
output_path = "output_image_with_fixed_frame.png"  # Path to save the output image

process_frame_and_image(frame_path, image_url, output_path)


Logs:

2024-12-02 01:29:29,058 - INFO - Loading the frame image...
2024-12-02 01:29:29,350 - INFO - Frame dimensions: Width=4867, Height=2300
2024-12-02 01:29:29,350 - INFO - Fetching the image from the URL...
2024-12-02 01:29:29,667 - INFO - Image dimensions: Width=600, Height=300
2024-12-02 01:29:29,667 - INFO - Cropping frame: Left=2133, Top=1000, Right=2733, Bottom=1300
2024-12-02 01:29:29,673 - INFO - New frame dimensions: Width=600, Height=300
2024-12-02 01:29:29,673 - INFO - Frame resized to match the image dimensions.
2024-12-02 01:29:29,674 - INFO - Combining the frame and the image...
2024-12-02 01:29:29,677 - INFO - Saving the result...
2024-12-02 01:29:29,699 - INFO - Result saved successfully: output_image_with_fixed_frame.png
2024-12-02 01:29:29,699 - INFO - Displaying the result...
