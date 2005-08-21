package net.sourceforge.stripes.examples.bugzooky.web;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.examples.bugzooky.biz.Attachment;
import net.sourceforge.stripes.examples.bugzooky.biz.Bug;
import net.sourceforge.stripes.examples.bugzooky.biz.BugManager;
import net.sourceforge.stripes.examples.bugzooky.biz.ComponentManager;
import net.sourceforge.stripes.examples.bugzooky.biz.PersonManager;
import net.sourceforge.stripes.validation.PercentageTypeConverter;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validatable;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import net.sourceforge.stripes.validation.ValidationErrors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Aug 20, 2005 Time: 12:26:03 PM To change this
 * template use File | Settings | File Templates.
 */
@UrlBinding("/bugzooky/SingleBug.action")
public class SingleBugActionBean extends BugzookyActionBean implements Validatable {
    private Bug bug;
    private FileBean newAttachment;

    /** Gets the bug for this Action. */
    @ValidateNestedProperties({
        @Validate(field="shortDescription", required=true),
        @Validate(field="longDescription", required=true),
        @Validate(field="percentComplete", converter=PercentageTypeConverter.class)
    })
    public Bug getBug() { return bug; }

    /** Sets the bug for this Action. */
    public void setBug(Bug bug) { this.bug = bug; }

    public FileBean getNewAttachment() { return newAttachment; }
    public void setNewAttachment(FileBean newAttachment) { this.newAttachment = newAttachment; }

    /** Does some very basic custom validation. */
    public void validate(ValidationErrors errors) {
        Float percentComplete = this.bug.getPercentComplete();
        if (percentComplete != null && percentComplete > 1) {
            SimpleError error = new SimpleError("Percent complete cannot be over 100%.");
            error.setFieldName("bug.percentComplete");
            errors.add("bug.percentComplete", error);
        }
    }

    /**
     * Loads a bug on to the form ready for editing.
     */
    @DontValidate
    @HandlesEvent("PreEdit")
    public Resolution loadBugForEdit() {
        BugManager bm = new BugManager();
        this.bug = bm.getBug( this.bug.getId() );
        return new ForwardResolution("/bugzooky/AddEditBug.jsp");
    }

    /** Saves (or updates) a bug, and then returns the user to the bug list. */
    @HandlesEvent("SaveOrUpdate") @DefaultHandler
    public Resolution saveOrUpdate() throws IOException {
        BugManager bm = new BugManager();
        ComponentManager cm = new ComponentManager();
        PersonManager pm = new PersonManager();

        Bug newBug = populateBug(this.bug);
        if (this.newAttachment != null) {
            Attachment attachment = new Attachment();
            attachment.setName(this.newAttachment.getFileName());
            attachment.setSize(this.newAttachment.getSize());

            BufferedReader reader = new BufferedReader
                    ( new InputStreamReader(this.newAttachment.getInputStream()) );
            StringBuilder builder = new StringBuilder();
            String line;

            while ( (line = reader.readLine()) != null ) {
                builder.append(line);
            }

            attachment.setData(builder.toString());
            newBug.addAttachment(attachment);
        }

        bm.saveOrUpdate(newBug);

        return new ForwardResolution("/bugzooky/BugList.jsp");
    }

    /** Saves or updates a bug, and then returns to the edit page to add another just like it. */
    @HandlesEvent("SaveAndAgain")
    public Resolution saveAndAddAnother() throws IOException {
        saveOrUpdate();

        return new RedirectResolution("/bugzooky/AddEditBug.jsp");
    }
}
