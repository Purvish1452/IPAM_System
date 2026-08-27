package com.motadata.traceorg.ipam.services.impl;

import com.motadata.traceorg.ipam.entity.settings.TraceOrgRoleFeaturePermission;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUserRole;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import com.motadata.traceorg.ipam.controller.settings.TraceOrgUserController;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
@org.springframework.stereotype.Service("userDetailsService")
public class TraceOrgUserDetailsService implements UserDetailsService 
{
	@Autowired
    private TraceOrgService traceOrgService;

	private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgUserController.class, "User Details Service");

	/**
	 * IPAM-147
	 * IPAM Roadmap : Admin should be able to create Users and should be able to give specific role based access rights to specific user.
	 * Created permission based access token so we can check authority using @PreAuthorize on each controller for granular control on each api.
	 */
	@SuppressWarnings("unchecked")
    @Transactional
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException 
    {
		TraceOrgUser traceOrgUser = traceOrgService.findByUserName(username);
    	
    	List<String> authorityList = new ArrayList<>();
    	
    	if(traceOrgUser != null)
    	{
    		try
			{
				TraceOrgUserRole traceOrgUserRole = (TraceOrgUserRole)traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_USERROLE, traceOrgUser.getUserRoleId().getId());

				if(traceOrgUserRole != null)
				{
					Set<TraceOrgRoleFeaturePermission> roleFeaturePermissions=traceOrgUser.getUserRoleId().getRoleFeaturePermissions();

					for (TraceOrgRoleFeaturePermission permission : roleFeaturePermissions)
					{
						if(permission.isReadPermission())
						{
							authorityList.add("PERM_"+permission.getFeature().getName()+"_READ");
						}
						if (permission.isWritePermission())
						{
							authorityList.add("PERM_"+permission.getFeature().getName()+"_WRITE");
						}
					}

					authorityList.add(traceOrgUserRole.getRole().toUpperCase());
				}
			}
			catch (Exception exception)
			{
				_logger.error(exception);
			}

    		List<GrantedAuthority> authorities = buildUserAuthority(authorityList);

			authorities.add(new SimpleGrantedAuthority("ROLE_"+traceOrgUser.getUserRoleId().getRole().toUpperCase()));

    		return buildUserForAuthentication(traceOrgUser, authorities);
    	}
    	else 
    	{
    		return null;
    	}    	
    }

    private static org.springframework.security.core.userdetails.User buildUserForAuthentication(TraceOrgUser traceOrgUser, List<GrantedAuthority> authorities)
    {
        return new org.springframework.security.core.userdetails.User(traceOrgUser.getUserName(), traceOrgUser.getPassword(), true, true, true, true, authorities);
    }
    
    private static List<GrantedAuthority> buildUserAuthority(List<String> authorityList)
    {
        Set<GrantedAuthority> setAuthentications = new HashSet<>();

        for (String authority : authorityList) 
        {
			if(authority.startsWith("PERM_"))
			{
				setAuthentications.add(new SimpleGrantedAuthority(authority));
			}
		}

		return new ArrayList<>(setAuthentications);
    }

}