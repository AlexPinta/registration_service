package com.registration.service;

import com.google.gson.Gson;
import com.registration.dao.UserDao;
import com.registration.core.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.io.File;

@Service
@Configurable
@Transactional
public class JmsServiceImpl implements JmsService {
    final String DESTINATION_QUEUE = "email-confirmation";
    final Gson gson = new Gson();

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private EmailService emailService;

//    @Autowired
//    UserDao userDao;

    public JmsServiceImpl() {
        FileSystemUtils.deleteRecursively(new File("activemq-data"));
    }

    @Override
    @JmsListener(destination = DESTINATION_QUEUE)
//    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED,
//                    rollbackFor = DataAccessException.class)
    public void receiveMessage(String message, Session session) {
        User user = gson.fromJson(message, User.class);
        try {
            if (emailService.isServiceAccessible()) {
                //userDao.createUser(user.getEmail(), user.getPassword());
                if (emailService.sendConfirmEmail(user)) {
                    session.commit();
                } else {
                    session.rollback();
                }
            }
        } catch (JMSException e) {
            //TODO logging can\'t rollback
        } finally {
            try {
                session.close();
            } catch (JMSException e) {
                //e.printStackTrace();
            }
        }

    }
    @Override
    public void sendMessage(final User user) {
        this.jmsTemplate.send(DESTINATION_QUEUE, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(gson.toJson(user));
            }
        });
    }
}