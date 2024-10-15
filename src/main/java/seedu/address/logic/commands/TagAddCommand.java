package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Tag;

/**
 * Adds a tag to an existing person in the address book.
 */
public class TagAddCommand extends Command {

    public static final String COMMAND_WORD = "tag-add";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds a tag to the person identified "
            + "by their name. "
            + "Parameters: "
            + "n/NAME t/[TAG]\n"
            + "Example: " + COMMAND_WORD + " n/ Li Sirui "
            + "Jane and Tom 230412";

    public static final String MESSAGE_ARGUMENTS = "Name: %1$s, Tag: %2$s";
    public static final String MESSAGE_ADD_TAG_SUCCESS = "Added tag to Person: %1$s";
    public static final String MESSAGE_ADD_TAG_FAILURE = "Tag must be a non-empty string with"
            + " only alphanumeric characters and underscores.";

    // All tags in existing Person match those to be added by the user
    public static final String MESSAGE_ALL_DUPLICATE_TAGS = "Contact '%1$s' already has the tag '%2$s'";

    // Some tags in existing Person match those to be added by the user
    public static final String MESSAGE_SOME_DUPLICATE_TAGS = "Contact '%1$s' already has the tag '%2$s'";



    private final Name name;
    private final Set<Tag> tags;
    private final Set<Tag> duplicateTags;

    /**
     * @param name of the person in the person list to edit the tags
     * @param tags of the person to be updated to
     */
    public TagAddCommand(Name name, Set<Tag> tags) {
        requireAllNonNull(name, tags);

        this.name = name;
        this.tags = tags;
        this.duplicateTags = new HashSet<>();
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        List<Person> matchingPersons = model.getFilteredPersonList().stream()
                .filter(person -> person.getName().fullName.equalsIgnoreCase(name.toString()))
                .toList();

        Person personToEdit = matchingPersons.get(0);

        Set<Tag> editedTags = handleDuplicateTags(personToEdit);

        Person editedPerson = new Person(
                personToEdit.getName(), personToEdit.getPhone(), personToEdit.getEmail(),
                personToEdit.getAddress(), personToEdit.getJob(), editedTags);

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        return new CommandResult(generateSuccessMessage(editedPerson));
    }

    /*
     * duplicate handling same tag
     * 
     * Cases:
     * add multiple tags with the same name using tag-add (tag-add n/name t/tag
     * t/tag)
     * add new tag with same tag name as existing tag (tag-add n/name t/tag) 
     *      either all tags match
     *      or one of the new tags match
     *          either mention the tags that match and add the ones that dont
     */

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof TagAddCommand)) {
            return false;
        }

        TagAddCommand e = (TagAddCommand) other;
        return tags.equals(e.tags);
    }

    /**
     * Generates a command execution success message based on whether
     * the tag is successfully added.
     * {@code personToEdit}.
     */
    private String generateSuccessMessage(Person personToEdit) {
        String message = duplicateTags.isEmpty() ? MESSAGE_ADD_TAG_SUCCESS : MESSAGE_ADD_TAG_FAILURE;
        return String.format(message, Messages.format(personToEdit));
    }

    /**
     * Handles duplicate tags as inputted by the user.
     * Checks if any of the tags from user input matches existing tags in the original Person.
     * Matching tags are added to a separate set that keeps track of duplicate tags.
     * Unique tags are added to a separate tag.
     * @param person EditedPerson from the execute method above.
     * @return A Set<Tag> that contains distinct tags from both
     * existing tags and those as inputted by the user.
     */
    private Set<Tag> handleDuplicateTags(Person person) {
        Set<Tag> ogTags = person.getTags();
        Set<Tag> mergedTags = new HashSet<>();

        for (Tag newTag : tags) {
            if (!ogTags.contains(newTag)) {
                mergedTags.add(newTag);
            } else {
                duplicateTags.add(newTag);
            }
        }

        for (Tag tag : ogTags) {
            if (!mergedTags.contains(tag)) {
                mergedTags.add(tag);
            }
        }

        return mergedTags;
    }
}
