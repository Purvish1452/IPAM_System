import json
import logging
import sys
import os
try:
    from base64 import decodebytes as decodestring
except ImportError:
    from base64 import decodestring

sys.path.insert(0,os.getcwd()+os.sep+'python engine'+os.sep+'com'+os.sep+'motadata'+os.sep+'traceorg'+os.sep+'python')

from exception import TraceOrgPythonException
from winrmclient import TraceOrgWinRMUtil

logging.basicConfig(filename=os.getcwd()+os.sep+'log'+os.sep+'motadata-python-engine.log', format='%(asctime)s %(levelname)s %(message)s',
                    datefmt='%d-%b-%Y %I:%M:%S %p', level=logging.WARN)

logger = logging.getLogger('windowsDHCP')


def collect(winrm_client, context):
    try:
        result = {}

        scope_output = ''

        if context.get('scope-ids') is not None and len(context.get('scope-ids')) > 0:

            for scope_id in str(context.get('scope-ids')).split(','):

                output = winrm_client.execute_command('Get-DhcpServerv4Reservation -ScopeId ' + scope_id)

                if output is not None and len(output) > 0:
                    scope_output += output

            if len(scope_output) > 0:
                result['dhcp-scope-reservations'] = scope_output

            scope_output = ''

            for scope_id in str(context.get('scope-ids')).split(','):

                output = winrm_client.execute_command('Get-DhcpServerv4Lease -ScopeId ' + scope_id)

                if output is not None and len(output) > 0:
                    scope_output += output

            if len(scope_output) > 0:
                result['dhcp-server-lease'] = scope_output

            scope_output = ''

            for scope_id in str(context.get('scope-ids')).split(','):

                output = winrm_client.execute_command('Get-DhcpServerv4PolicyIPRange -ScopeId ' + scope_id)

                if output is not None and len(output) > 0:
                    scope_output += output

            if len(scope_output) > 0:
                result['dhcp-scope-range-policies'] = scope_output

        else:

            output = winrm_client.execute_command('Get-DhcpServerv4Statistics')

            if output is not None and len(output) > 0:
                result['dhcp-server-statistics'] = output

            output = winrm_client.execute_command('Get-DhcpServerv4Scope')

            if output is not None and len(output) > 0:
                result['dhcp-scopes'] = output

            output = winrm_client.execute_command('Get-DhcpServerv4ScopeStatistics')

            if output is not None and len(output) > 0:
                result['dhcp-scope-statistics'] = output

    except Exception as exception:
        TraceOrgPythonException.log_error_exception()

    return result



def discover(winrm_client, context):
    try:
        result = {}

        status = winrm_client.execute_command('Get-Service -Name DHCPServer | select-object Status')

        if "Running" in status:

            output = winrm_client.execute_command('Get-DhcpServerv4Statistics | select-object ServerStartTime')
            if output is not None and len(output) > 0:
                result['result'] = output

            return result
        else:
            context['error-code'] = "DHCP Server Service Stopped"

    except Exception as exception:
        TraceOrgPythonException.log_error_exception()

    return None


def get_winrm_client(context):
    try:
        port = 5985

        end_point = ''

        if context.get('port') is not None:
            port = int(context.get('port'))

        if port == 5985:
            end_point = "http://" + context.get('host') + ":5985/wsman"
        elif port == 5986:
            end_point = "https://" + context.get('host') + ":5986/wsman"

        context['endpoint'] = end_point

        winrm_client = TraceOrgWinRMUtil(context['endpoint'],
                                         context['username'],
                                         context['password'],
                                         context['timeout'])

        winrm_client.init()

        return winrm_client

    except Exception as exception:
        context['error-code'] = exception.__class__.__name__
        TraceOrgPythonException.log_error_exception()

    return None

if sys.argv[1] == 'collector':
    try:
        context = json.loads(decodestring(sys.argv[2]))

        winrm_client = get_winrm_client(context)

        if winrm_client is not None and winrm_client.connected:
            result = collect(winrm_client, context)

            if result is not None:
                context['result'] = result
            else:
                context['error-code'] = winrm_client.exception.__class__.__name__
        else:
            context['error-code'] = winrm_client.exception.__class__.__name__
        winrm_client.destroy()

        del context['password']

        del context['username']

    except Exception as exception:
        context['error-code'] = exception.__class__.__name__
        TraceOrgPythonException.log_error_exception()
    finally:

        print json.dumps(context)

else:
        try:
            context = json.loads(decodestring(sys.argv[2]))

            winrm_client = get_winrm_client(context)

            if winrm_client is not None and winrm_client.connected:
                result = discover(winrm_client, context)

                if result is not None:
                    context['result'] = result
                else:
                    if context.get('error-code') is None:
                        if winrm_client.exception is not None:
                            context['error-code'] = winrm_client.exception.__class__.__name__
                        else:
                            context['error-code'] = "Failed to discover"
            else:
                if context.get('error-code') is None:
                    if winrm_client.exception is not None:
                        context['error-code'] = winrm_client.exception.__class__.__name__
                    else:
                        context['error-code'] = "Failed to discover"
            winrm_client.destroy()

            del context['password']

            del context['username']

        except Exception as exception:
            context['error-code'] = exception.__class__.__name__
            TraceOrgPythonException.log_error_exception()
        finally:
            print json.dumps(context)