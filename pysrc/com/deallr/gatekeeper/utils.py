__author__ = 'prachi'

import re

# Module for all generic util methods
class Utils:

    # Looks at the incoming email address and verifies if its in a valid format
    @staticmethod
    def validateEmail(address):
        pattern = "^[a-zA-Z0-9._%-+]+@[a-zA-Z0-9._%-]+.[a-zA-Z]{2,6}$"
        if re.match(pattern, address):
            return True
        else:
            return False