package net.sourceforge.stripes.examples.bugzooky.web;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.examples.bugzooky.biz.Bug;
import net.sourceforge.stripes.examples.bugzooky.biz.BugManager;
import net.sourceforge.stripes.examples.bugzooky.biz.ComponentManager;
import net.sourceforge.stripes.examples.bugzooky.biz.PersonManager;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import net.sourceforge.stripes.validation.ValidationErrors;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@UrlBinding("/bugzooky/MultiBug.action")
public class MultiBugActionBean extends BugzookyActionBean {
    /** Populated during bulk add/edit operations. */
    private List<Bug> bugs = new ArrayList<Bug>();

    /** Populated by the form submit on the way into bulk edit. */
    private int[] bugIds;

    /** Gets the array of bug IDs the user selected for edit. */
    public int[] getBugIds() { return bugIds; }

    /** Sets the array of bug IDs the user selected for edit. */
    public void setBugIds(int[] bugIds) { this.bugIds = bugIds; }

    @ValidateNestedProperties({
        @Validate(field="shortDescription", required=true, maxlength=75),
        @Validate(field="longDescription", required=true, minlength=25),
        @Validate(field="component.id", required=true),
        @Validate(field="owner.id", required=true),
        @Validate(field="status", required=true),
        @Validate(field="priority", required=true)
    })
    public List<Bug> getBugs() {
        return bugs;
    }

    public void setBugs(List<Bug> bugs) {
        this.bugs = bugs;
    }

    @DefaultHandler
    @HandlesEvent("SaveOrUpdate")
    public Resolution saveOrUpdate() {
        BugManager bm = new BugManager();
        PersonManager pm = new PersonManager();
        ComponentManager cm = new ComponentManager();

        for (Bug bug : bugs) {
            Bug newBug = populateBug(bug);
            bm.saveOrUpdate(newBug);
        }

        return new ForwardResolution("/bugzooky/BugList.jsp");
    }

    @DontValidate
    @HandlesEvent("PreEdit")
    public Resolution preBulkEdit() {
        // If the user didn't select any bugs to edit, bad user.
        if (this.bugIds == null || this.bugIds.length < 1) {            
            ValidationErrors errors = new ValidationErrors();
            errors.add( new SimpleError("You must select at least one bug to edit.") );
            getContext().setValidationErrors(errors);
            return getContext().getSourcePageResolution();
        }

        BugManager bm = new BugManager();
        for (int id : this.bugIds) {
            this.bugs.add( bm.getBug(id) );
        }

        return new ForwardResolution("/bugzooky/BulkAddEditBugs.jsp");
    }
}
