package com.KBP.Evidence.PojoClasses;

/**
 * Created by Niranjan on 12/7/2015.
 */
public class Nell_Extracts {

    private String relation;

    private String mentions;

    private String object;

    public Nell_Extracts(String relation, String mentions, String object) {
        this.relation = relation;
        this.mentions = mentions;
        this.object = object;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getMentions() {
        return mentions;
    }

    public void setMentions(String mentions) {
        this.mentions = mentions;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }


    @Override
    public String toString() {
        return "Nell_Extracts{" +
                "relation='" + relation + '\'' +
                ", mentions='" + mentions + '\'' +
                ", object='" + object + '\'' +
                '}';
    }
}
