'''
1.0 existing first version
'''
import linecache
import logging
import sys

logger = logging.getLogger('exception')


class TraceOrgPythonException:
    def __init__(self):
        pass

    @staticmethod
    def log_error_exception():
        exc_type, exc_obj, tb = sys.exc_info()
        f = tb.tb_frame
        lineno = tb.tb_lineno
        filename = f.f_code.co_filename
        linecache.checkcache(filename)
        line = linecache.getline(filename, lineno, f.f_globals)
        logger.error('EXCEPTION IN ({}, LINE {} "{}"): {}'.format(filename, lineno, line.strip(), exc_obj))
