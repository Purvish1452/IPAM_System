import ipaddress
import json
import sys
import os
import logging
from pysnmp.hlapi import *
from pysnmp.smi import builder
from pysnmp.entity.rfc3413.oneliner import cmdgen

current_dir = os.getcwd()
os.chdir('..')
parent_dir = os.getcwd()
sys.path.insert(0, parent_dir + os.sep + 'python-engine' + os.sep + 'com' + os.sep + 'motadata' + os.sep + 'traceorg' + os.sep + 'python')

logging.basicConfig(filename=parent_dir + os.sep + 'log' + os.sep + 'motadata-python-engine.log',
                    format='%(asctime)s %(levelname)s %(message)s',
                    datefmt='%d-%b-%Y %I:%M:%S %p',
                    level=logging.INFO)
logger = logging.getLogger('Remote subnet snmp walk')

def is_valid_subnet(subnet, mask):
    try:
        # Create an IPv4 network object
        network = ipaddress.IPv4Network(f"{subnet}/{mask}", strict=False)

        # Check if the subnet is a valid network start address
        if network.network_address != ipaddress.IPv4Address(subnet):
            return False

        # Check if the mask is /32 (255.255.255.255) and return False
        if network.prefixlen == 32:
            return False

        return True
    except ValueError:
        return False

if sys.argv[1] is not None:
    context = str(sys.argv[1])
    credential = json.loads(context)
    version = credential.get('version')
    gateway = credential.get('gateway')

    if version in ['v1', 'v2c']:
        community = credential.get('community', 'public')
        mpModel = 0 if version == 'v1' else 1
        auth = cmdgen.CommunityData(community, mpModel=mpModel)
    elif version == 'v3':
        userName = credential.get('user-name', '')
        authKey = credential.get('auth-password', '').encode('utf-8')
        privKey = credential.get('private-password', '').encode('utf-8')
        auth_protocol_map = {
            'MD5': cmdgen.usmHMACMD5AuthProtocol,
            'SHA': cmdgen.usmHMACSHAAuthProtocol,
            'SHA224': cmdgen.usmHMAC128SHA224AuthProtocol,
            'SHA256': cmdgen.usmHMAC192SHA256AuthProtocol,
            'SHA384': cmdgen.usmHMAC256SHA384AuthProtocol,
            'SHA512': cmdgen.usmHMAC384SHA512AuthProtocol
        }
        authProtocol = auth_protocol_map.get(credential.get('auth-protocol'), cmdgen.usmHMACMD5AuthProtocol)
        priv_protocol_map = {
            'DES': cmdgen.usmDESPrivProtocol,
            '3DES': cmdgen.usm3DESEDEPrivProtocol,
            'AES': cmdgen.usmAesCfb128Protocol,
            'AES128': cmdgen.usmAesCfb128Protocol,
            'AES192': cmdgen.usmAesCfb192Protocol,
            'AES256': cmdgen.usmAesCfb256Protocol
        }
        privProtocol = priv_protocol_map.get(credential.get('privacy-protocol'), cmdgen.usmDESPrivProtocol)
        security_level = credential.get('security-level', 'noAuthNoPriv')
        if security_level == 'noAuthNoPriv':
            auth = cmdgen.UsmUserData(userName)
        elif security_level == 'authNoPriv':
            auth = cmdgen.UsmUserData(userName, authKey, authProtocol)
        elif security_level == 'authPriv':
            auth = cmdgen.UsmUserData(userName=userName, authKey=authKey, authProtocol=authProtocol, privKey=privKey, privProtocol=privProtocol)

    transport = cmdgen.UdpTransportTarget((gateway, 161), timeout=60, retries=1)
    snmp_engine = SnmpEngine()

    oids = [
        ObjectType(ObjectIdentity('1.3.6.1.2.1.4.21.1.11')),  #OID for IP Route Table's Subnet Mask
        ObjectType(ObjectIdentity('1.3.6.1.4.1.9.2.4.2.1.1'))  #OID for Cisco's Subnet Mask
    ]

    results = {}
    for oid in oids:
        for (errorIndication, errorStatus, errorIndex, varBinds) in bulkCmd(snmp_engine, auth, transport, ContextData(), 0, 100, oid, lexicographicMode=False, ignoreNonIncreasingOid=True):
            if errorIndication or errorStatus:
                logger.error("Error " + str(errorIndication))
                logger.error("Error Status " + str(errorStatus))
                break
            else:
                for varBind in varBinds:
                    oid_str, value = varBind
                    subnet = oid_str.prettyPrint().split('.')[-4:]
                    subnet = '.'.join(subnet)
                    subnet_mask = value.prettyPrint()
                    if is_valid_subnet(subnet, subnet_mask):
                        results[subnet] = subnet_mask

    context = {'result': results}
    logger.info("Result of snmpwalk on gateway: [" + gateway + "] : " + str(json.dumps(context)))
    print(json.dumps(context))
    sys.exit()