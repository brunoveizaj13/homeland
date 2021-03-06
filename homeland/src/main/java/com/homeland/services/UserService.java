package com.homeland.services;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.homeland.assemblers.Assembler;
import com.homeland.assemblers.DomainAssembler;
import com.homeland.assemblers.RequestAssembler;
import com.homeland.constants.IStatus;
import com.homeland.dto.UserDTO;
import com.homeland.entities.Login;
import com.homeland.entities.User;
import com.homeland.exceptions.EntityExistsException;
import com.homeland.exceptions.NoContentException;
import com.homeland.exceptions.NotFoundException;
import com.homeland.models.Principal;
import com.homeland.models.UserToken;
import com.homeland.repositories.UserRepository;
import com.homeland.requests.api.UserRequest;
import com.homeland.requests.repository.UserSQL;
import com.homeland.utils.StringUtil;

@Service
public class UserService {

	@Autowired
	UserRepository userDAO;
	@Autowired
	TokenService tokenService;
	
	public List<UserDTO> searchUser(UserRequest req, Integer userId)
	{
		UserSQL criterias = new RequestAssembler().apiToSql(req);
		List<User> users = userDAO.searchUser(criterias);
		return new Assembler().userListToDto(users);
	}
	
	public UserDTO findUserById(Integer userId, Integer authUserId)
	{
		if(userId == null) return null;
		
		return new Assembler().toDto(userDAO.findById(userId));
	}
	
	public UserDTO findUserByUsername(String username, Integer userId)
	{
		if(!StringUtil.isValid(username)) return null;
		
		UserRequest req = new UserRequest(username);
		
		List<UserDTO> users = searchUser(req, userId);
		
		if(users!=null && !users.isEmpty())
		{
			return users.get(0);
		}
		
		return null;
	}
	
	@Transactional
	public UserDTO registerUser(UserDTO dto,Integer userId)
	{
		
		if(dto == null) return null;
		
		if(!StringUtil.isValid(dto.getUsername()))
		{
			throw new NoContentException("Plotesoni 'Perdoruesin'");
		}
		
		if(!StringUtil.isValid(dto.getSecret()))
		{
			throw new NoContentException("Plotesoni 'Fjalekalimin'");
		}
		
		if(!StringUtil.isValid(dto.getRole()))
		{
			throw new NoContentException("Plotesoni 'Rolin'");
		}
		
		if(findUserByUsername(dto.getUsername(),userId) != null)
		{
			throw new EntityExistsException("Perdoruesi ekziston");
		}
		
		User u = new DomainAssembler().toDomain(null,dto);
		u.setCreatedTime(new Date());
		u.setStatus(IStatus.ACTIVE);
		u.setCreatedUserId(userId);
		
		return new Assembler().toDto(userDAO.create(u));
		
	}
	
	@Transactional
	public UserDTO modifyUser(UserDTO dto,Integer userId)
	{
		
		if(dto == null) return null;
		
		if(!StringUtil.isValid(dto.getUsername()))
		{
			throw new NoContentException("Plotesoni 'Perdoruesin'");
		}
		
		if(!StringUtil.isValid(dto.getSecret()))
		{
			throw new NoContentException("Plotesoni 'Fjalekalimin'");
		}
		
		if(!StringUtil.isValid(dto.getRole()))
		{
			throw new NoContentException("Plotesoni 'Rolin'");
		}
		
		
		List<User> usrList = userDAO.searchUser(new RequestAssembler().apiToSql(new UserRequest(dto.getUsername())));
		if(usrList == null || usrList.isEmpty())
		{
			throw new NotFoundException("Ky perdorues nuk ekziston");
		}
		
		User usr = usrList.get(0);
		
		if(usr.getId() != dto.getId())
		{
			throw new EntityExistsException("Perdoruesi '"+dto.getUsername()+"' eshte i zene");
		}
		
		User u = new DomainAssembler().toDomain(usr, dto);
		u.setModifyTime(new Date());
		u.setModifyUserId(userId);
				
		return new Assembler().toDto(userDAO.update(u));
		
	}
	
	
	@Transactional
	public UserToken login(Principal principal)
	{
		String username = principal.getUsername();
		String password = principal.getPassword();
		
		
		User u = userDAO.findByUsername(username);
		
		if(u == null) throw new NotFoundException("Perdoruesi nuk ekziston");
		
		if(u.getStatus() != IStatus.ACTIVE) 
		{
			throw new NotFoundException("Perdoruesi nuk eshte aktiv");
		}
		
		if(!password.equals(u.getSecret()))
		{
			throw new NotFoundException("Fjalekalimi i gabuar");
		}
		
		
		
		String token = tokenService.generateToken(new Assembler().toDto(u));
		
		Login login = new Login();
		login.setBrowser(principal.getBrowser());
		login.setIp(principal.getIp());
		login.setLoginTime(Calendar.getInstance().getTime());
		login.setToken(token);
		login.setUserId(u.getId());
		login.setUsername(u.getUsername());
		
		userDAO.create(login);
		
		return new UserToken(new Assembler().toDto(u),token);
		
	}
	
	
	
}
