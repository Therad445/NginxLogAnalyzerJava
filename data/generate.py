#!/usr/bin/env python3
"""Minimal traffic generator using Poisson arrivals."""
# MIT License
import time
import requests
import random

RATE = 1  # requests per second

while True:
    start = time.time()
    try:
        requests.get('http://localhost')
    except Exception:
        pass
    wait = random.expovariate(RATE)
    elapsed = time.time() - start
    if wait > elapsed:
        time.sleep(wait - elapsed)
