package com.example.jobtracker;

public class JobDataModel
{
    String company;
    String position;
    String dateOfSubmission;;
    String dateOfReply;
    String status;
    String key;
    String jobType;
    String salary;

    public JobDataModel()
    {

    }
    public JobDataModel(String company,String position,String status,String salary,String jobType,
                        String key,String dateOfSubmission,String dateOfReply)
    {
        this.company = company;
        this.position = position;
        this.dateOfSubmission = dateOfSubmission;
        this.status = status;
        this.salary = salary;
        this.jobType = jobType;
        this.key = key;
        this.dateOfReply = dateOfReply;


    }
    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDateOfSubmission() {
        return dateOfSubmission;
    }

    public void setDateOfSubmission(String dateOfSubmission) {
        this.dateOfSubmission = dateOfSubmission;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getSalary() {
        return salary;
    }
    public void setSalary(String salary) {
        this.salary = salary;
    }
    public String getJobType() {
        return jobType;
    }
    public void setJobType(String jobType) {
        this.jobType = jobType;
    }
    public String getDateOfReply() { return dateOfReply; }
    public void setDateOfReply(String dateOfReply) { this.dateOfReply = dateOfReply; }
}
