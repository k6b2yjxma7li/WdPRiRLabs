from bs4 import BeautifulSoup
import requests
import numpy

import re
import shutil
# using multiprocessing because python multithreading is still 
# bound to a single core (python GIL) so it switches between threads instead
# of running in parallel
import multiprocessing
import datetime

import os

url = "https://www.if.pw.edu.pl/~mrow/dyd/wdprir/"

# setting up bs to parse the page
response = requests.get(url)
soup = BeautifulSoup(response.content.decode('utf-8'), 'lxml')

# find png images hidden in <a href>...</a> tags
png_names = [ahref['href'] for ahref in soup.find_all('a', href=re.compile(".*png.*"))]
# prepare full urls of images
png_urls = [url+png_name for png_name in png_names]

# # Part 1: sequential download
# start_time = datetime.datetime.now()
# for png_url, png_name in zip(png_urls, png_names):
#     resp_png = requests.get(png_url, stream=True)
#     if resp_png.status_code == 200:
#         with open(png_name, "wb") as img:
#             resp_png.raw.decode_content = True
#             shutil.copyfileobj(resp_png.raw, img)
# end_time = datetime.datetime.now()
# print(f"Sequential download of {len(png_names)} png images finished in: {end_time-start_time}")

# Part 2: pooled download
def download_png(url, filepath, *args):
    try:
        resp_png = requests.get(url, stream=True)
        if resp_png.status_code == 200:
            with open(filepath, "wb") as img:
                resp_png.raw.decode_content = True
                shutil.copyfileobj(resp_png.raw, img)
    except Exception:
        return False
    finally:
        return True

start_time = datetime.datetime.now()
pool_size = multiprocessing.cpu_count()
with multiprocessing.Pool(pool_size) as pool:
    pool.starmap(download_png, zip(png_urls, png_names))
end_time = datetime.datetime.now()
print(f"Process-pooled download of {len(png_names)} png images finished in: {end_time-start_time}")

