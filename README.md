# Shamir’s Secret Sharing & Homomorphic Image Downscaling

This project implements a (2,3)-threshold Shamir Secret Sharing scheme applied to BMP images, alongside a demonstration of homomorphic image downscaling performed on encrypted image shares.

## Features

- Splits a BMP image into 3 shares using Shamir’s Secret Sharing while preserving the original file header.
- Reconstructs the original image using any 2 of the generated shares.
- Performs image downscaling by a factor of 2 on both the original image and the encrypted shares.
- Demonstrates the homomorphic property: downscaling encrypted shares followed by reconstruction produces an image closely matching the directly downscaled original.
- Calculates Mean Average Error (MAE) between the downscaled original and reconstructed images to quantify accuracy.
- Includes detailed instructions and sample images for easy replication.

## How to Run

1. Prepare a BMP image with a 54-byte header.
2. Run the program to generate 3 shares from the original image.
3. Perform downscaling on the original image and all three shares.
4. Reconstruct the downscaled image using any 2 of the downscaled shares.
5. Review the output images and MAE value to evaluate correctness.

## Technologies Used

- Java
- Image processing libraries (Java AWT/Swing or Python PIL)
- Cryptographic concepts including Shamir’s Secret Sharing and homomorphic encryption principles

## Project Structure

- `src/` — Source code files
- `images/` — Sample input and output BMP images

## Author
Lakshay Bansal


