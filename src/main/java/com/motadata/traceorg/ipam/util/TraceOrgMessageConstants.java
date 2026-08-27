package com.motadata.traceorg.ipam.util;

/**
 * @author Krunal Thakkar
 *
 */

public interface TraceOrgMessageConstants
{
    String ENTER_VALID_DETAILS ="Enter Valid Details";

    String SOMETHING_WENT_WRONG = "Something Went Wrong";

    String ENTER_VALID_TIMELINE ="Enter Valid Time";

    String AVAILABLE_IP_NOT_ROGUE = "Only Used IP can be mark as a Rogue IP";

    String AVAILABLE_IP_NOT_TRUST = "Only Used IP can be mark as a Trusted IP";

    String USER_NAME_ALREADY_EXIST = "Username already exist";

    String DHCP_CREDENTIAL_ALREADY_EXIST = "DHCP credential already exist";

    String DHCP_CREDENTIAL_NAME_ALREADY_EXIST = "DHCP credential name already exist";

    String DHCP_CREDENTIAL_DELETE_SUCCESS = "DHCP server deleted successfully";

    String USER_DELETE_SUCCESS = "User deleted successfully";

    String REPORT_SCHEDULER_DELETE_SUCCESS = "Scheduler deleted successfully";

    String USER_ADD_SUCCESS = "User added successfully";

    String MAIL_ADD_SUCCESS = "Mail ID added successfully";

    String DHCP_CREDENTIAL_ADD_SUCCESS = "DHCP server added successfully";

    String DHCP_CREDENTIAL_VALID = "DHCP server is valid";

    String USER_CAN_NOT_DELETE_OWN = "Current user can not be deleted";

    String USER_CAN_NOT_DISABLE_OWN = "Current user can not be disabled";

    String USER_ID_WRONG = "User Id is not valid";

    String CANT_DELETE_SUBNET_UNDER_SCAN = "Scanning is in Progress, So cannot delete the subnet";

    String CANT_DELETE_DHCP_UNDER_SCAN = "Scanning is in Progress, So cannot delete the DHCP Server";

    String CANT_DELETE_CATEGORY_UNDER_SCAN = "Scanning is in progress, so cannot delete the subnet category";

    String DO_NOT_HAVE_ACCESS = "User haven't any right to perform this operation";

    String USER_UPDATE_SUCCESS = "User updated successfully";

    String DHCP_UPDATE_SUCCESS = "DHCP server updated successfully";

    String USER_PASSWORD_UPDATE_SUCCESS = "Password changed successfully";

    String BRAND_UPDATE_SUCCESS = "Rebranding successfully";

    String MAIL_SERVER_NOT_VALID = "Mail server is invalid";

    String MAIL_SERVER_VALID = "Mail server is valid";

    String MAIL_SERVER_UPDATE_SUCCESS = "Mail server updated successfully";

    String GLOBAL_SETTING_UPDATE_SUCCESS = "Global setting updated successfully";

    String DATABASE_MAINTENANCE_UPDATE_SUCCESS = "Database Maintenance updated successfully";

    String MAIL_SERVER_ID_NOT_VALID = "Mail server id is not valid";

    String TOKEN_NOT_RECOGNISED = "Token was not recognised";

    String TOKEN_NULL = "Token is null";

    String TOKEN_EXPIRED = "Token is expired";

    String SUBNET_ADDRESS_ALREADY_EXIST = "Subnet address already exists";

    String SUBNET_NAME_ALREADY_EXIST = "Subnet name already exists";

    String SUBNET_NAME_AND_ADDRESS_ALREADY_EXIST = "Subnet name and address already exists";

    String SCHEDULER_ADD_SUCCESS = "Scheduler added successfully";

    String SCHEDULER_UPDATE_SUCCESS = "Scheduler updated successfully";

    String SUBNET_ID_NOT_VALID = "Subnet id is not valid";

    String DHCP_ID_NOT_VALID = "DHCP Credential is not valid";

    String SUBNET_IP_ID_NOT_VALID = "Subnet IP id is not valid";

    String IP_ID_NOT_VALID = "IP id is not valid";

    String SUBNET_DETAIL_NOT_VALID = "Subnet details is not valid";

    String CSV_IMPORT_SUCCESS = "CSV File imported successfully";

    String SUBNET_DETAIL_VALID = "Subnet details are valid";

    String SUBNET_ADD_SUCCESS = "Subnet added successfully";

    String SUBNET_UPDATE_SUCCESS = "Subnet updated successfully";

    String SUBNET_DELETE_SUCCESS = "Subnet deleted successfully";

    String CATEGORY_DELETE_SUCCESS = "Category deleted successfully";

    String SUBNET_IP_DELETE_SUCCESS = "IP Address deleted successfully";

    String SUBNET_IP_ROGUE_SUCCESS = "IP Address mark as rogue successfully";

    String SUBNET_IP_TRUST_SUCCESS = "IP Address mark as trusted successfully";

    String CATEGORY_ADD_SUCCESS = "Category added successfully";

    String CATEGORY_UPDATE_SUCCESS = "Category updated successfully";

    String CATEGORY_NAME_ALREADY_EXIST = "Category name already exist";

    String CATEGORY_ID_NOT_VALID = "Category id is not valid";

    String BAD_CREDENTIAL = "Login failed: User is not authenticated. Please check credentials and try again.";

    String SUBNET_IP_ADD_SUCCESS = "IP Addresses added successfully";

    String SUBNET_IP_UPDATE_SUCCESS = "IP Address updated successfully";

    String SUBNET_SCAN_ALREADY_RUNNING = "Scan is already running";

    String SCAN_ALREADY_RUNNING = "Scan is already running";

    String SUBNET_SCAN_STARTED = "Scan started successfully";

    String IMPORT_RUNNING = "Please wait for some time, Import is running";

    String DHCP_SCAN_STARTED = "DHCP scan started successfully";

    String DHCP_SCAN_SUCCESS = "DHCP server scanned successfully";

    String FILE_NOT_VALID = "File Type is not valid";

    String EMPTY_FILE = "File is Empty";

    String FILE__DETAIL_NOT_VALID = "File details is not valid";

    String NO_DATA_AVAILABLE = "No Data Available";

    String MARKED_AUTHENTICITY_FAIL = "update Authenticity Status Failed";

    String DELETE_MAC_ADDRESSES_FAIL = "Delete MAC Addresses Failed";

    String DELETE_MAC_ADDRESSES_SUCCESS = "Delete MAC Addresses Successfully";

    String DELETE_MAC_ADDRESSES_NON_DISCOVERED_AUTHENTICITY_SUCCESS = "Delete MAC Addresses Authenticity and Marked as Discovered";

    String TRUSTED_MAC_ADDRESSES_IMPORT_SUCCESS = "Trusted MAC Addresses Imported Successfully";

    String EXPORT_ROGUE_DETECTION_SUCCESS = "Rogue Detection is exported Successfully";

    String ROLE_ALREADY_EXIST = "Role already exist";

    String ROLE_DELETE_SUCCESS = "Role deleted successfully";

    String ROLE_ADD_SUCCESS = "Role added successfully";

    String ROLE_UPDATED_SUCCESS = "Role updated successfully";

    String ROLE_ID_WRONG = "Role Id is not valid";

    String REMOVE_USERS = "Remove associated users and try again!";

    String ENTER_VALID_ROLE_NAME ="Role Name cannot be empty";

    String SUPERNET_ADDED_SUCCESS = "Supernet is added Successfully";

    String SUPERNET_DELETE_SUCCESS = "Supernet deleted successfully";

    String SUPERNET_EXIST = "Supernet is Exist";

    String SUPERNET_NOT_EXIST = "Supernet does not Exist";

    String SUPERNET_MASK_INVALID = "Enter Supernet Mask between 8 to 23";

    String INVALID_SUPERNET = "Invalid Supernet IP Address";

    String VALID_SUPERNET_WITH_CIDR = "Valid Supernet IP Address with Entered CIDR is ";

    String IPV4_ADDRESS_ONLY = "Enter valid Ipv4 address only";

    String IP_REQUEST_APPROVED="Ip request approved successfully";

    String IP_REQUEST_REJECTED="Ip request rejected successfully";

    String IP_REQUEST_ID_WRONG = "Ip request id is not valid";

    String ALLOCATE_IP="IP allocation request cannot be approved as no specific IP was selected from the subnet.";

    String IP_REQUEST_ADDED="Ip request added successfully";

    String SELECT_EXACT_NO_OF_IPS="Select the exact number of IPs as specified in \"No. of IPs\".";

    String IPS_NOT_AVAILABLE = "The requested IPs are not fully available. Please assign alternative ones.";

    String CUSTOM_COLUMN_ADD_SUCCESS = "Custom Column added successfully";

    String CUSTOM_COLUMN_DELETE_SUCCESS = "Custom Column deleted successfully";

    String COLUMN_ALREADY_EXIST = "Column name already exist";

    String INVALID_CSV_HEADER = "Invalid CSV header. Please ensure the header matches the required format.";

    String MAX_CUSTOM_COLUMN="At each level, only 10 custom columns are allowed.";
}
