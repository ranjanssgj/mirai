import requests
import json
import sys

def find_extension(url, query):
    try:
        response = requests.get(url)
        data = response.json()
        for ext in data:
            if query.lower() in ext['name'].lower() or query.lower() in ext['pkg'].lower():
                print(f"Found {query}:")
                print(json.dumps(ext, indent=2))
                return
        print(f"Not found: {query}")
    except Exception as e:
        print(f"Error: {e}")

print("Searching for animekai...")
find_extension("https://raw.githubusercontent.com/yuzono/anime-repo/repo/index.min.json", "animekai")

print("\nSearching for comix...")
find_extension("https://raw.githubusercontent.com/yuzono/manga-repo/repo/index.min.json", "comix")

print("\nSearching for comick...")
find_extension("https://raw.githubusercontent.com/yuzono/manga-repo/repo/index.min.json", "comick")
