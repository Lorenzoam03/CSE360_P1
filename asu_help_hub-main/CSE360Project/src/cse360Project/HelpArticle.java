package cse360Project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>HelpArticle Class</p>
 *
 * <p>Description: Represents a help article within the application, containing all necessary information such as header, title, descriptions, keywords, body content, links, and associated groups.</p>
 *
 * <p>Copyright: Lorenzo Martinez © 2024</p>
 *
 * @author 
 *
 * @version 1.0.0 2024-10-29 Updated for Phase 2
 *
 */
public class HelpArticle implements Serializable {
    private static final long serialVersionUID = 1L;

    // Unique long integer identifier
    private long id;
    private String level;               // Article level (beginner, intermediate, advanced, expert)
    private String header;
    private String title;
    private String shortDescription;
    private String bodyContent;
    private String accessRestrictions;  // Who can read the article
    private String sensitiveTitle;      // For sensitive information handling
    private String sensitiveDescription;
    private List<String> keywords;
    private List<String> links;
    private List<String> groups;

    // Constructors

    /**
     * Default constructor initializes lists to prevent NullPointerException.
     */
    public HelpArticle() {
        this.keywords = new ArrayList<>();
        this.links = new ArrayList<>();
        this.groups = new ArrayList<>();
    }

    /**
     * Parameterized constructor for creating a HelpArticle with all fields specified.
     *
     * @param id                   Unique identifier
     * @param level                Article level
     * @param header               Header information
     * @param title                Title of the article
     * @param shortDescription     Short description of the article
     * @param bodyContent          Main content of the article
     * @param accessRestrictions   Access restrictions for the article
     * @param sensitiveTitle       Sensitive title information
     * @param sensitiveDescription Sensitive description information
     * @param keywords             List of keywords
     * @param links                List of related links
     * @param groups               List of groups/categories
     */
    public HelpArticle(long id, String level, String header, String title, String shortDescription,
                       String bodyContent, String accessRestrictions, String sensitiveTitle,
                       String sensitiveDescription, List<String> keywords, List<String> links, List<String> groups) {
        this.id = id;
        this.level = level;
        this.header = header;
        this.title = title;
        this.shortDescription = shortDescription;
        this.bodyContent = bodyContent;
        this.accessRestrictions = accessRestrictions;
        this.sensitiveTitle = sensitiveTitle;
        this.sensitiveDescription = sensitiveDescription;
        this.keywords = keywords != null ? keywords : new ArrayList<>();
        this.links = links != null ? links : new ArrayList<>();
        this.groups = groups != null ? groups : new ArrayList<>();
    }

    // Getters and Setters for all fields

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public void setBodyContent(String bodyContent) {
        this.bodyContent = bodyContent;
    }

    public String getAccessRestrictions() {
        return accessRestrictions;
    }

    public void setAccessRestrictions(String accessRestrictions) {
        this.accessRestrictions = accessRestrictions;
    }

    public String getSensitiveTitle() {
        return sensitiveTitle;
    }

    public void setSensitiveTitle(String sensitiveTitle) {
        this.sensitiveTitle = sensitiveTitle;
    }

    public String getSensitiveDescription() {
        return sensitiveDescription;
    }

    public void setSensitiveDescription(String sensitiveDescription) {
        this.sensitiveDescription = sensitiveDescription;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords != null ? keywords : new ArrayList<>();
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links != null ? links : new ArrayList<>();
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups != null ? groups : new ArrayList<>();
    }

    // Additional Methods

    /**
     * Adds a keyword to the article.
     *
     * @param keyword The keyword to add
     */
    public void addKeyword(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty() && !this.keywords.contains(keyword)) {
            this.keywords.add(keyword);
        }
    }

    /**
     * Removes a keyword from the article.
     *
     * @param keyword The keyword to remove
     */
    public void removeKeyword(String keyword) {
        this.keywords.remove(keyword);
    }

    /**
     * Adds a link to the article.
     *
     * @param link The link to add
     */
    public void addLink(String link) {
        if (link != null && !link.trim().isEmpty() && !this.links.contains(link)) {
            this.links.add(link);
        }
    }

    /**
     * Removes a link from the article.
     *
     * @param link The link to remove
     */
    public void removeLink(String link) {
        this.links.remove(link);
    }

    /**
     * Adds a group to the article.
     *
     * @param group The group to add
     */
    public void addGroup(String group) {
        if (group != null && !group.trim().isEmpty() && !this.groups.contains(group)) {
            this.groups.add(group);
        }
    }

    /**
     * Removes a group from the article.
     *
     * @param group The group to remove
     */
    public void removeGroup(String group) {
        this.groups.remove(group);
    }

    // Override toString() method for debugging purposes

    @Override
    public String toString() {
        return "HelpArticle{" +
                "id=" + id +
                ", level='" + level + '\'' +
                ", header='" + header + '\'' +
                ", title='" + title + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", bodyContent='" + bodyContent + '\'' +
                ", accessRestrictions='" + accessRestrictions + '\'' +
                ", sensitiveTitle='" + sensitiveTitle + '\'' +
                ", sensitiveDescription='" + sensitiveDescription + '\'' +
                ", keywords=" + keywords +
                ", links=" + links +
                ", groups=" + groups +
                '}';
    }
}
