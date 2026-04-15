package com.projmodel.plugin.service.impl;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.user.ApplicationUser;
import com.projmodel.plugin.service.IssueDataService;

import javax.inject.Named;
import com.atlassian.query.Query;
import java.util.Collections;
import java.util.List;

@Named
public class IssueDataServiceImpl implements IssueDataService {

    private final SearchService _searchService;

    @Override
    public List<Issue> getIssuesForProject(String projectKey) {
        return List.of();
    }

    @Override
    public List<Issue> getOpenIssuesForProject(String projectKey) {
        return List.of();
    }

    private List<Issue> searchIssuesByJql(String jql) {
        try {
            ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
            if(user == null) {
                return Collections.emptyList();
            }

            SearchService.ParseResult parseResult = _searchService.parseQuery(user, jql);
            if(!parseResult.isValid()) {
                return Collections.emptyList();
            }

            Query q = parseResult.getQuery();
            SearchResults<Issue> results = _searchService.search(user, q, com.atlassian.jira.web.bean.PagerFilter.getUnlimitedFilter());

            return results.getResults();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
