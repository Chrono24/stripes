<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>


<div class="sectionTitle">
    <c:choose>
        <c:when test="${form != null}">Edit Bug</c:when>
        <c:otherwise>Add Bug</c:otherwise>
    </c:choose>
</div>

<jsp:useBean id="componentManager" scope="page"
             class="net.sourceforge.stripes.examples.bugzooky.biz.ComponentManager"/>
<jsp:useBean id="personManager" scope="page"
             class="net.sourceforge.stripes.examples.bugzooky.biz.PersonManager"/>

<stripes:form action="/bugzooky/SingleBug.action">
    <stripes:errors/>

    <table class="display">
        <tr>
            <th>Bug ID:</th>
            <td>
                ${form.bug.id}
                <stripes:hidden name="bug.id"/>
            </td>
        </tr>
        <tr>
            <th>Opened On:</th>
            <td><fmt:formatDate value="${form.bug.openDate}" dateStyle="medium"/></td>
        </tr>
        <tr>
            <th>Component:</th>
            <td>
                <stripes:select name="bug.component.id">
                    <stripes:options-collection collection="${componentManager.allComponents}"
                                                label="name" value="id"/>
                </stripes:select>
            </td>
        </tr>
        <tr>
            <th>Assigned To:</th>
            <td>
                <stripes:select name="bug.owner.id">
                    <stripes:options-collection collection="${personManager.allPeople}"
                                                label="username" value="id"/>
                </stripes:select>
            </td>
        </tr>
        <tr>
            <th>Priority:</th>
            <td>
                <stripes:select name="bug.priority">
                    <stripes:options-enumeration enum="net.sourceforge.stripes.examples.bugzooky.biz.Priority"/>
                </stripes:select>
            </td>
        </tr>
        <tr>
            <th>Status:</th>
            <td>
                <stripes:select name="bug.status">
                    <stripes:options-enumeration enum="net.sourceforge.stripes.examples.bugzooky.biz.Status"/>
                </stripes:select>
            </td>
        </tr>
        <tr>
            <th>Due Date:</th>
            <td><stripes:text name="bug.dueDate" formatPattern="medium"/></td>
        </tr>
        <tr>
            <th>Percent Complete:</th>
            <td><stripes:text name="bug.percentComplete" formatType="percentage"/></td>
        </tr>
        <tr>
            <th>Short Description:</th>
            <td><stripes:text size="75" name="bug.shortDescription"/></td>
        </tr>
        <tr>
            <th>Long Description:</th>
            <td><stripes:textarea cols="75" rows="10" name="bug.longDescription"/></td>
        </tr>
        <tr>
            <th>Attachments:</th>
            <td>
                <c:forEach items="${form.bug.attachments}" var="attachment" varStatus="loop">
                    ${attachment.name} (${attachment.size} bytes) -
                    <em>${attachment.preview}...</em><br/>
                </c:forEach>

                Add a new attachment: <stripes:file name="newAttachment"/>
            </td>
        </tr>
    </table>

    <div class="buttons">
        <stripes:submit name="SaveOrUpdate" value="Save and Return"/>
        <stripes:submit name="SaveAndAgain" value="Save and Add Another"/>
    </div>
</stripes:form>
<%@ include file="footer.jsp" %>
