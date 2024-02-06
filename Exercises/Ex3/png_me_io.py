from bs4 import BeautifulSoup
import requests
import numpy as np
import scipy

import re
import shutil
import io
from PIL import Image
# using multiprocessing because python multithreading is still 
# bound to a single core (python GIL) so it switches between threads instead
# of running in parallel
import multiprocessing
import datetime

import os

url = "https://www.if.pw.edu.pl/~mrow/dyd/wdprir/"

# setting up bs to parse the page
response = requests.get(url)
soup = BeautifulSoup(response.content.decode('utf-8'))

# find png images hidden in <a href>...</a> tags
png_names = [ahref['href'] for ahref in soup.find_all('a', href=re.compile(".*png.*"))]
# prepare full urls of images
png_urls = [url+png_name for png_name in png_names]

# Part 1: sequential download
start_time = datetime.datetime.now()
for png_url, png_name in zip(png_urls, png_names):
    resp_png = requests.get(png_url, stream=True)
    if resp_png.status_code == 200:
        with open(png_name, "wb") as img:
            resp_png.raw.decode_content = True
            shutil.copyfileobj(resp_png.raw, img)
end_time = datetime.datetime.now()
print(f"Sequential download of {len(png_names)} png images finished in: {end_time-start_time}")

# # Part 2: pooled download
def download_png(url, filepath, *args):
    resp_png = requests.get(url, stream=True)
    if resp_png.status_code == 200:
        with open(filepath, "wb") as png_file:
            with io.BytesIO(resp_png.content) as png_stream:
                with Image.open(png_stream) as pil_image:
                    pil_image.save(png_file, format='png')
    return True

start_time = datetime.datetime.now()
pool_size = multiprocessing.cpu_count()
with multiprocessing.Pool(pool_size) as pool:
    pool.starmap(download_png, zip(png_urls, map(lambda s: s.replace(".", ".pool."), png_names)))
end_time = datetime.datetime.now()
print(f"Process-pooled download of {len(png_names)} png images finished in: {end_time-start_time}")

# Part 3: sequential download with filter
gauss = np.array([
    [1/16, 1/8, 1/16],
    [1/8,  1/4, 1/8],
    [1/16, 1/8, 1/16],
])
start_time = datetime.datetime.now()
for png_url, png_name in zip(png_urls, map(lambda s: s.replace(".", ".blur."), png_names)):
    resp_png = requests.get(png_url, stream=True)
    if resp_png.status_code == 200:
        with open(png_name, "wb") as png_file:
            with io.BytesIO(resp_png.content) as png_stream:
                with Image.open(png_stream) as pil_image:
                    # load image into array to apply blur
                    pil_image_array = np.array(pil_image)
                    # convolving one color at a time
                    for layer in range(3):
                        pil_image_array[:,:,layer] = scipy.signal.convolve2d(pil_image_array[:,:,layer], gauss, mode='same')
                    with Image.fromarray(pil_image_array) as blurred_image:
                        blurred_image.save(png_file, format="png")
end_time = datetime.datetime.now()
print(f"Sequential download and blur of {len(png_names)} png images finished in: {end_time-start_time}")

# Part 4: pooled download
def download_and_filter_png(url, filepath, *args):
    gauss = np.array([
        [1/16, 1/8, 1/16],
        [1/8,  1/4, 1/8],
        [1/16, 1/8, 1/16],
    ])
    resp_png = requests.get(url, stream=True)
    if resp_png.status_code == 200:
        with open(filepath, "wb") as png_file:
            with io.BytesIO(resp_png.content) as png_stream:
                with Image.open(png_stream) as pil_image:
                    # load image into array to apply blur
                    pil_image_array = np.array(pil_image)
                    # convolving one color at a time
                    for layer in range(3):
                        pil_image_array[:,:,layer] = scipy.signal.convolve2d(pil_image_array[:,:,layer], gauss, mode='same')
                    with Image.fromarray(pil_image_array) as blurred_image:
                        blurred_image.save(png_file, format="png")
    return True

start_time = datetime.datetime.now()
pool_size = multiprocessing.cpu_count()
with multiprocessing.Pool(pool_size) as pool:
    pool.starmap(download_and_filter_png, zip(png_urls, map(lambda s: s.replace(".", ".blur.pool."), png_names)))
end_time = datetime.datetime.now()
print(f"Process-pooled and blurred download of {len(png_names)} png images finished in: {end_time-start_time}")