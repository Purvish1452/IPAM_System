import json
import logging
import sys
try:
    from base64 import decodebytes as decodestring
except ImportError:
    from base64 import decodestring

from com.motadata.traceorg.python.collector import collect
from com.motadata.traceorg.python.discovery import discover

logging.basicConfig(filename='./log/motadata-python-engine.log', format='%(asctime)s %(levelname)s %(message)s',
                    datefmt='%d-%b-%Y %I:%M:%S %p', level=logging.WARN)

if sys.argv[1] == 'collector':
    collect(json.loads(decodestring(sys.argv[2])))
else:
    discover(json.loads(decodestring(sys.argv[2])))
