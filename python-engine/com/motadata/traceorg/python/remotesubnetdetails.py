import json
import sys
import os
import logging

from datetime import datetime
from pysnmp.hlapi import *
from pysnmp.smi import builder
from pysnmp.entity.rfc3413.oneliner import cmdgen

current_dir = os.getcwd()

os.chdir('..')

parent_dir = os.getcwd()

sys.path.insert(0, parent_dir + os.sep + 'python-engine' + os.sep + 'com' + os.sep + 'motadata' + os.sep + 'traceorg' + os.sep + 'python')

logging.basicConfig(filename=parent_dir+os.sep+'log'+os.sep+'motadata-python-engine.log', format='%(asctime)s %(levelname)s %(message)s',
                    datefmt='%d-%b-%Y %I:%M:%S %p', level=logging.INFO)

logger = logging.getLogger('Remote subnet snmp walk')

mibBuilder = builder.MibBuilder()

MacAddress, = mibBuilder.importSymbols('SNMPv2-TC', 'MacAddress')

rows = []

gateway_address = []

if sys.argv[1] is not None and sys.argv[2] is not None and sys.argv[3] is not None:

    context = str(sys.argv[1])

    subnet_ip = str(sys.argv[2])

    cidr = sys.argv[3]

    now = datetime.now()

    current_time = now.strftime("%H:%M:%S")

    logger.info("Execution started at : " + current_time)

    credential = json.loads(context)

    sub_stri = "."

    val = -1

    occurrence = 3

    for i in range(0, occurrence):

        if subnet_ip.find(sub_stri, val + 1) == -1:

            context = {'result': rows}

            logger.info("Execution terminated abruptly as subnet value passed incorrectly : " + json.dumps(context))

            print(json.dumps(context))

            sys.exit()

        else:

            val = subnet_ip.find(sub_stri, val + 1)

    gateway_address.append(subnet_ip[:val+1])

    cideNum = int(cidr)

    if cideNum < 24 :

        val = -1

        for i in range(0,3):

            if i == 1:

                startVal = subnet_ip.find(sub_stri, val + 1)

                startVal = startVal + 1

            if i == 2:

                endVal = subnet_ip.find(sub_stri, val + 1)

            val = subnet_ip.find(sub_stri, val + 1)

        loop = 24 - cideNum

        subnet = int(subnet_ip[startVal:endVal])

        if loop == 1:

            subnet = int(subnet_ip[startVal:endVal]) + 1

            gateway_address.append(subnet_ip[:startVal] + str(subnet))

        else:

            endLoopIndex = pow(2, loop)

            for j in range(0,endLoopIndex):

                subnet = subnet + 1

                if j != endLoopIndex - 1:

                    gateway_address.append(subnet_ip[:startVal] + str(subnet) + ".")

    seen = set()

    version = credential.get('version')

    if version in ['v1', 'v2c']:

        # Handle SNMPv1 and SNMPv2c using CommunityData
        community = credential.get('community', 'public')

        mpModel = 0 if version == 'v1' else 1  # v1 = 0, v2c = 1

        auth = cmdgen.CommunityData(community, mpModel=mpModel)

    elif version == 'v3':

        # Handle SNMPv3 with UsmUserData
        userName = credential.get('user-name', '')

        authKey = credential.get('auth-password', '').encode('utf-8')  # Ensure it's in byte format

        privKey = credential.get('private-password', '').encode('utf-8')  # Ensure it's in byte format

        # Map auth-protocol from input to pysnmp supported protocols
        auth_protocol_map = {
            'MD5': cmdgen.usmHMACMD5AuthProtocol,
            'SHA': cmdgen.usmHMACSHAAuthProtocol,
            'SHA224': cmdgen.usmHMAC128SHA224AuthProtocol,
            'SHA256': cmdgen.usmHMAC192SHA256AuthProtocol,
            'SHA384': cmdgen.usmHMAC256SHA384AuthProtocol,
            'SHA512': cmdgen.usmHMAC384SHA512AuthProtocol
        }

        authProtocol = auth_protocol_map.get(credential.get('auth-protocol'), cmdgen.usmHMACMD5AuthProtocol)

        # Map privacy-protocol from input to pysnmp supported protocols
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

            auth = cmdgen.UsmUserData(userName=userName, authKey=authKey, authProtocol=authProtocol, privKey=privKey,
                                      privProtocol=privProtocol)

    # Define the transport target (host and port)
    transport = cmdgen.UdpTransportTarget((credential.get('gateway'), 161), timeout=60, retries=1)

    # Create SNMP command generator
    snmp_engine = SnmpEngine()

    oid = ObjectType(ObjectIdentity('1.3.6.1.2.1.4.22.1.2'))

    logger.info("Starting snmpwalk on gateway: [" + credential.get("gateway") + "] for OID '1.3.6.1.2.1.4.22.1.2'")

    errorCount = 0

    for (errorIndication,
         errorStatus,
         errorIndex,
         varBinds) in bulkCmd(snmp_engine,
                              auth,
                              transport,
                              ContextData(), 0, 100,
                              oid,
                              lexicographicMode=False,
                              ignoreNonIncreasingOid=True):

        if errorIndication or errorStatus:

            logger.info("Error " + str(errorIndication))

            logger.info("Error Status " + str(errorStatus))

            errorCount = errorCount + 1

            if 'timeout' in str(errorIndication).lower():

                break

            if errorCount == 25:

                logger.info("Error detected for many times hence terminating execution")

                break

            continue

        else:

            for varBind in varBinds:

                try:

                    if "=" in str(varBind):

                        prettyPrint = varBind.prettyPrint()

                        if 'mib' in prettyPrint.lower():

                            for single_gateway_address in gateway_address:

                                if single_gateway_address in prettyPrint:

                                    count = 0

                                    for x in varBind:

                                        isIgnored = 'false'

                                        count = count + 1

                                        if count % 2 == 0:

                                            macInString = str(x.prettyPrint())

                                            octetString = macInString[2:]

                                            if not '0x' in macInString or '0X' in macInString:

                                                isIgnored = 'true'

                                                continue

                                            if octetString.find('.') == -1:

                                                formattedMac = (MacAddress(hexValue=octetString.zfill(12))).prettyPrint()

                                            else:

                                                logger.info("skipped ip due to incorrect val : " + octetString)

                                                continue
                                        else:

                                            occurrence = 4

                                            sub_str = "."

                                            val = -1

                                            output = prettyPrint

                                            ipAddress = output

                                            for i in range(0, occurrence):

                                                val = output.rfind(sub_str)

                                                output = output[:val]

                                            ipAddress = ipAddress[val + 1:]

                                            ipAddress = ipAddress[:ipAddress.find('=') - 1]

                                    if ipAddress not in seen and 'true' != isIgnored:

                                        logger.info("seen ip : " + ipAddress)

                                        seen.add(ipAddress)

                                    else:

                                        logger.info("skipped ip : " + ipAddress)

                                        continue

                                    if formattedMac != "00:00:00:00:00:00":

                                        row = {ipAddress: formattedMac}

                                        rows.append(row)
                except:

                    logger.info("Exception executing : " + varBind.prettyPrint())

context = {'result': rows}

logger.info("Result of snmpwalk on gateway: [" + str(credential.get("gateway")) + "] for OID '1.3.6.1.2.1.4.22.1.2' : " + str(json.dumps(context)))

now = datetime.now()

current_time = now.strftime("%H:%M:%S")

logger.info("Execution completed at : " + current_time)

print(json.dumps(context))

sys.exit()
