package org.osc.manager.ism.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.osc.manager.ism.model.Domain;
import org.osc.manager.ism.model.DomainListElement;
import org.osc.sdk.manager.api.ManagerDomainApi;
import org.osc.sdk.manager.element.ApplianceManagerConnectorElement;

public class IsmDomainApi implements ManagerDomainApi {

    Logger log = Logger.getLogger(IsmDomainApi.class);

    public IsmDomainApi(ApplianceManagerConnectorElement mc) throws Exception {

    }

    public static IsmDomainApi create(ApplianceManagerConnectorElement mc) throws Exception {
        return new IsmDomainApi(mc);
    }

    @Override
    public Domain getDomain(String domainId) throws Exception {
        return new Domain(0L, "Root-Domain");
    }

    @Override
    public List<DomainListElement> listDomains() throws Exception {
        List<DomainListElement> domainList = new ArrayList<DomainListElement>();
        domainList.add(new DomainListElement(0L, "Root-Domain"));
        return domainList;
    }

}
