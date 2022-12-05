package org.start2do;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.snmp4j.CommandResponder;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.start2do.config.SNMPConfig;
import org.start2do.config.SNMPConfig.V3Config;

@EnableAutoConfiguration
@Import(SNMPConfig.class)
@ConditionalOnProperty(prefix = "snmp", name = "enable", havingValue = "true")
@RequiredArgsConstructor
public class AutoSNMPConfig {


    public static void main(String[] args) {
//        CommunityTarget

        PDU pdu = new PDU();
        //GET 用来得到一条管理信息
        pdu.setType(PDU.GETNEXT);
    }

    @ConditionalOnProperty(prefix = "snmp", value = "version", havingValue = "3")
    @ConditionalOnMissingBean(value = Snmp.class)
    public static class V3Class {

        @Bean
        public Snmp createCommunityTarget(SNMPConfig config, UsmUser user, CommandResponder responder)
            throws IOException {
            Address address = GenericAddress.parse(config.getListen());
            Snmp snmp;
            if (address instanceof UdpAddress) {
                snmp = new Snmp(new DefaultUdpTransportMapping((UdpAddress) address));
            } else {
                snmp = new Snmp(new DefaultTcpTransportMapping((TcpAddress) address));
            }
            snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
            snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
            OctetString localEngineID = new OctetString(MPv3.createLocalEngineID());
            USM usm = new USM(SecurityProtocols.getInstance().addDefaultProtocols(), localEngineID, 0);
            usm.addUser(user);
            snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3(usm));
            snmp.addCommandResponder(responder);
            snmp.listen();
            return snmp;
        }

        @Bean
        @ConditionalOnMissingBean(value = {UsmUser.class})
        public UsmUser usmUser(SNMPConfig config) {
            V3Config v3Config = config.getV3();
            return new UsmUser(new OctetString(v3Config.getUser()), new OID(v3Config.getAuthGeneric()),
                new OctetString(v3Config.getPassword()), new OID(v3Config.getPrivacyProtocol()),
                new OctetString(v3Config.getPrivacyPassphrase()));
        }
    }

    @ConditionalOnMissingBean(value = Snmp.class)
    @AutoConfigureAfter(V3Class.class)
    public static class V1V2Class {

        @Bean
        public Snmp createCommunityTarget(SNMPConfig config, CommandResponder responder) throws IOException {
            Address address = GenericAddress.parse(config.getListen());
            Snmp snmp;
            if (address instanceof UdpAddress) {
                snmp = new Snmp(new DefaultUdpTransportMapping((UdpAddress) address));
            } else {
                snmp = new Snmp(new DefaultTcpTransportMapping((TcpAddress) address));
            }
            snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
            snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
            snmp.addCommandResponder(responder);
            snmp.listen();
            return snmp;
        }
    }
}
