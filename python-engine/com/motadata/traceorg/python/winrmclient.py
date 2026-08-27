'''
1.0 existing first version
1.1 execute_script method added
'''

import logging
import base64

from winrm import Protocol

logger = logging.getLogger('winrm')


class TraceOrgWinRMUtil:
    def __init__(self, endpoint, username, password, timeout):
        self.connection = None
        self.shell = None
        self.exception = None
        self.endpoint = endpoint
        self.username = username
        self.password = password
        self.connected = False
        self.timeout = timeout

    def init(self):
        try:
            logger.debug("creating winrm connection " + self.endpoint)
            self.connection = Protocol(endpoint=self.endpoint, transport='ntlm', username=self.username,
                                       password=self.password, server_cert_validation='ignore',
                                       read_timeout_sec=int(self.timeout) + 1, operation_timeout_sec=int(self.timeout))
            self.shell = self.connection.open_shell()
            if self.shell is not None:
                self.connected = True
                logger.debug("winrm connection created " + self.endpoint)
            else:
                logger.warn("failed to established winrm connection " + self.endpoint)

        except Exception as exception:
            self.exception = exception
            logger.warn(exception)

    def execute_command(self, command):

        result = None
        try:
            if self.connected:

                command = 'powershell -command "$Host.UI.RawUI.BufferSize = New-Object ' \
                          'Management.Automation.Host.Size (512,25);@@@ | Format-List"'.replace("@@@", command)
                logger.debug(command)
                command_id = self.connection.run_command(self.shell, command)
                if command_id is not None:
                    result, error, status = self.connection.get_command_output(self.shell, command_id)
                    logger.debug(result)
                    self.connection.cleanup_command(self.shell, command_id)
        except Exception as exception:
            print exception
        return result

    def execute_script(self, command):
        result = None

        try:
            if self.connected:

                command = '$Host.UI.RawUI.BufferSize = New-Object Management.Automation.Host.Size (512, 25);' + command +";$?"
                logger.debug(command)
                command = "powershell -encodedcommand "+base64.b64encode(command.encode("UTF-16LE"))
                command_id = self.connection.run_command(self.shell, command)
                if command_id is not None:
                    result, error, status = self.connection.get_command_output(self.shell, command_id)
                    logger.debug(result)
                    self.connection.cleanup_command(self.shell, command_id)
        except Exception as exception:
            print exception
        return result

    def destroy(self):
        try:
            if self.connected:
                self.connection.close_shell(self.shell)
                logger.debug("winrm connection destroyed " + self.endpoint)
        except Exception as exception:
            print exception
