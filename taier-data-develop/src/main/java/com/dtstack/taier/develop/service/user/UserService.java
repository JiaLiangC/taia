/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtstack.taier.develop.service.user;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dtstack.taier.common.enums.Deleted;
import com.dtstack.taier.common.env.EnvironmentContext;
import com.dtstack.taier.common.exception.ErrorCode;
import com.dtstack.taier.common.exception.TaierDefineException;
import com.dtstack.taier.common.util.RSAUtil;
import com.dtstack.taier.dao.domain.LdapGroup;
import com.dtstack.taier.dao.domain.User;
import com.dtstack.taier.dao.domain.po.DaoPageParam;
import com.dtstack.taier.dao.domain.po.DsListBO;
import com.dtstack.taier.dao.dto.UserDTO;
import com.dtstack.taier.dao.mapper.UserMapper;
import com.dtstack.taier.dao.pager.PageResult;
import com.dtstack.taier.datasource.api.exception.SourceException;
import com.dtstack.taier.datasource.plugin.common.utils.DBUtil;
import com.dtstack.taier.develop.bo.datasource.DsListParam;
import com.dtstack.taier.develop.controller.user.UserController;
import com.dtstack.taier.develop.vo.datasource.DsListVO;
import com.dtstack.taier.develop.vo.user.UserListVO;
import com.dtstack.taier.pluginapi.util.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public static final int IS_ADMIN  = 1;

    @Resource(name="ldapDataSource")
    private DataSource dataSource;

    private static String privateKey = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIICXQIBAAKBgQDbOYcY8HbDaNM9ooYXoc9s+R5oR05ZL1BsVKadQBgOVH/kj7PQ\n" +
            "uD+ABEFVgB6rJNi287fRuZeZR+MCoG72H+AYsAhRsEaB5SuI7gDEstXuTyjhx5bz\n" +
            "0wUujbDK4VMgRfPO6MQo+A0c95OadDEvEQDG3KBQwLXapv+ZfsjG7NgdawIDAQAB\n" +
            "AoGAQqPgL3KZh5lL7YaEIJbtiQDJf4V9iZraZbPt2gtrxJ9nKUGNtbrsgqvIeIcz\n" +
            "y26t+h9oF3bFYLD7jwbZ9DOIWSin7NJ1RumRT/GN+i3qJfuLdTDywRG0wIiSIJR+\n" +
            "0jz/nG6QOW199waXMbgjTd/+FlEMfz0traqHQgIZFDkU/7ECQQD4j+/qM/922Ado\n" +
            "l6zvg8Z2uqEpEF0SH0l0+x8qsL2S9NjLZWgTZLiTLv3vxnA/kGCfBo/pNtskkuEx\n" +
            "3iTaSG8fAkEA4cjbJqcKCkxKW3gAm8OZCH9O04UzaowsHW4UsNwFkFqdoGg8q017\n" +
            "2W3Vc6xH4vD/1hhme+OANqyaktU4fm9kNQJBAI7g7mAKE8cU1u1ggqALd4G4NfuM\n" +
            "1HMeWPNNhtTbU52t8RC58eFz/EVetcmmn89qBqBi/UZpqf6UD67CqxxulrECQFXi\n" +
            "UkJcrbwHEw3CEvEtMOwDiRd6hnlUAn/bXLF9r/weC/F1VQaQPbkSR2xtrxaLN7XX\n" +
            "qDwd6Kpjc5TA2HF3q7UCQQDfTOSOmq6JJzWUFY7s5ZoVPmvPgFxqwcysgnqbP2vp\n" +
            "iHbNRMYI+dvj6ppC4BujGm5Wczw7vDs0/M4jREE9eY3r\n" +
            "-----END RSA PRIVATE KEY-----";

    public String getUserName(Long userId) {
        User user = this.baseMapper.selectById(userId);
        return null == user ? "" : user.getUserName();
    }

    public Map<Long, User> getUserMap(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return new HashMap<>();
        }
        List<User> users = this.baseMapper.selectBatchIds(userIds);
        if (CollectionUtils.isEmpty(users)) {
            return new HashMap<>();
        }
        return users.stream().collect(Collectors.toMap(User::getId, u -> u));
    }

    public User getById(Long userId) {
        return this.baseMapper.selectById(userId);
    }


    public List<User> listAll() {
        return this.baseMapper.selectList(Wrappers.lambdaQuery(User.class).eq(User::getIsDeleted, Deleted.NORMAL.getStatus()));
    }

    public User getByUserName(String username) {
        return this.baseMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getUserName, username));
    }

    public UserDTO getUserByDTO(Long userId) {
        if (userId == null) {
            return null;
        }
        User one = getById(userId);
        if (Objects.isNull(one)) {
            return null;
        }
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(one, userDTO);
        return userDTO;
    }

    public User getUserByLdap(String username, String password) throws Exception {
        Map<String, String> user = new HashMap<>();
        user.put("username", username);
        user.put("password", password);

        String result = getLdapUser(user);
        logger.info("ldap user info : {}", result);
        if(result != null) {
            JSONObject data = JSONObject.parseObject(result);
            data.put("decryptPwd", password);
            return userConvert(data);
        }

        return null;
    }

    private String getLdapUser(Map<String,String> user) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String jsonString;
        try{
            String sql = "SELECT u.*, gu.group_id from users u left JOIN group_users gu on u.id = gu.user_id where username=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,user.get("username"));

            rs = pstmt.executeQuery();
            JSONObject jsonObject = new JSONObject();
            if (rs.next()){
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i=1;i<=columnCount;i++){
                    String columnName = metaData.getColumnName(i);
                    jsonObject.put(columnName,rs.getObject(columnName));
                }
            }else{
               return null;
            }
            jsonString = jsonObject.toString();
        } catch (Exception e) {
            throw new SourceException(String.format("SQL execute exception：%s", e.getMessage()), e);
        } finally {
            DBUtil.closeDBResources(rs,pstmt,conn);
        }

        return jsonString;
    }

    public List<User> getLdapUsers() throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<User> users = new ArrayList<>();
        try{
            String sql = "select id, username, nickname from users where status=1";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()){
               User user = new User();
               user.setId(rs.getLong("id"));
               user.setUserName(rs.getString("nickname"));
               users.add(user);
            }
        } catch (Exception e) {
            throw new SourceException(String.format("SQL execute exception：%s", e.getMessage()), e);
        } finally {
            DBUtil.closeDBResources(rs,pstmt,conn);
        }

        return users;
    }

    public List<LdapGroup> getLdapGroups() throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<LdapGroup> groups = new ArrayList<>();
        try{
            String sql = "select id, group_name from `groups` where parent_id <> 0";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()){
                LdapGroup user = new LdapGroup();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("group_name"));
                groups.add(user);
            }
        } catch (Exception e) {
            throw new SourceException(String.format("SQL execute exception：%s", e.getMessage()), e);
        } finally {
            DBUtil.closeDBResources(rs,pstmt,conn);
        }

        return groups;
    }

    public User userConvert(JSONObject data) throws Exception {
        Long userId = data.getLongValue("id");
        User user = getById(userId);
        String username = data.getString("mail");
        String password = data.getString("password");
        Long groupId = data.getLong("group_id");
        String decryptPwd = data.getString("decryptPwd");
        String ldapPwd = RSAUtil.decryptRSA(password,privateKey);
        String md5Password = MD5Util.getMd5String(decryptPwd);
        if (user == null && decryptPwd.equals(ldapPwd)) {
            user = new User();
            user.setIsAdmin(0);
            user.setId(userId);
            user.setUserName(username);
            user.setPhoneNumber(data.getString("mobile"));
            user.setEmail(username);
            user.setPassword(md5Password);
            this.baseMapper.insert(user);
            user = getById(userId);
            user.setPwdValid(true);
        }else if(user == null && !decryptPwd.equals(ldapPwd)) {
            user = new User();
            user.setPwdValid(false);
        } else if(user != null && !decryptPwd.equals(ldapPwd)) {
            user.setPwdValid(false);
        } else if(user != null && decryptPwd.equals(ldapPwd)) {
            user.setPwdValid(true);
        }
        user.setGroupId(groupId);
        return user;
    }

    public List<UserListVO> total(DsListParam dsListParam) {
        dsListParam.setCurrentPage(1);
        dsListParam.setPageSize(DaoPageParam.MAX_PAGE_SIZE);
        List<User> userList = this.baseMapper.selectList(Wrappers.lambdaQuery(User.class));
        List<UserListVO> uListVOS = new ArrayList<>();
        for (User user : userList) {
            if(user.getId()==1){
                continue;
            }
            UserListVO userListVO = new UserListVO();
            userListVO.setUserId(user.getId());
            userListVO.setUserName(user.getUserName());
            userListVO.setEmail(user.getEmail());
            uListVOS.add(userListVO);
        }
        return uListVOS;
    }

    public static void main(String[] args) throws Exception {
        String decryptPwd = RSAUtil.decryptRSA("rnGEXqnxGOjQMDj5FAk/Qki+qiZoajhTBcXBRjQbMrBvPXwH5zNnT1R5snzZxOypafsJKH8g3hm8YlEJHk+YLx7TxB2uOUcsb6QmpHbFsgm5sFjfx7Ivru5PaJiIbScvv4br92Gs/AW3Z0Gy1ivfkC9K4NQKtInH8LtkM7Rha0A=", privateKey);
        System.out.println(decryptPwd);
    }
}
