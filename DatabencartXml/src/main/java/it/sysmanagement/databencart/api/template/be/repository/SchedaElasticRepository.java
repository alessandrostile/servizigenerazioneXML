package it.sysmanagement.databencart.api.template.be.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.sysmanagement.databencart.api.template.be.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.elasticsearch.index.query.BoolQueryBuilder;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Repository
public class SchedaElasticRepository {

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	private static final int Default_Page = 0;
	@Value("${pdf.result.size.limit}")
	int Default_size;

	@Value("${elastic.databenc.content.index.name}")
	String contentIndex;

	private final String ELASTIC_SEARCH_TYPE_ORCHESTRATOR = "scheda"; // TODO Constants

	public String findSchedeByGroup(String group) throws JsonProcessingException {
		BoolQueryBuilder query = QueryBuilders.boolQuery();
		QueryBuilder queryForSchedeByGroup = null;

		queryForSchedeByGroup = QueryBuilders.termQuery(Constants.SCHEDA_DOMAIN_QUERY, group);
		query.filter(queryForSchedeByGroup);

		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query).withIndices(contentIndex)
				.withTypes(ELASTIC_SEARCH_TYPE_ORCHESTRATOR).withPageable(PageRequest.of(Default_Page, Default_size))
				.build();

		@SuppressWarnings("rawtypes")
		List<Map> map = null;
		map = elasticsearchTemplate.queryForPage(searchQuery, Map.class).getContent();

		ObjectMapper mapper = new ObjectMapper();
		String orderedResult = orderBy(mapper.writeValueAsString(map), Constants.TYPE_KEY);

		return orderedResult;
	}

	public String findSchedeByType(String TSK) throws JsonProcessingException {
		BoolQueryBuilder query = QueryBuilders.boolQuery();
		QueryBuilder queryForSchedeByType = null;
		@SuppressWarnings("unused")
		AbstractAggregationBuilder<?> aggregationByType = AggregationBuilders.terms(Constants.SCHEDA_DOMAIN);

		queryForSchedeByType = QueryBuilders.termQuery(Constants.TYPE_KEY_PATH_QUERY, TSK);

		query.filter(queryForSchedeByType);

		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query).withIndices(contentIndex)
				.withTypes(ELASTIC_SEARCH_TYPE_ORCHESTRATOR).withPageable(PageRequest.of(Default_Page, Default_size))
				.build();

		@SuppressWarnings("rawtypes")
		List<Map> map = null;
		map = elasticsearchTemplate.queryForPage(searchQuery, Map.class).getContent();
		ObjectMapper mapper = new ObjectMapper();
		String orderedResult = orderBy(mapper.writeValueAsString(map), Constants.SCHEDA_DOMAIN);
		return orderedResult;
	}

	public String findSchedeByGroupAndType(String group, String TSK, Boolean isBatch) throws JsonProcessingException {
		BoolQueryBuilder query = QueryBuilders.boolQuery();
		QueryBuilder queryForSchedeByType = null;
		QueryBuilder queryForSchedeByGroup = null;
		SearchQuery searchQuery = null;

		if (group != null) {
			queryForSchedeByGroup = QueryBuilders.termQuery(Constants.SCHEDA_DOMAIN_QUERY, group);
			query.filter(queryForSchedeByGroup);
		}
		if (TSK != null) {
			queryForSchedeByType = QueryBuilders.termQuery(Constants.TYPE_KEY_PATH_QUERY, TSK);
			query.filter(queryForSchedeByType);
		}
		if (isBatch == null || (isBatch != null && !isBatch)) {
			searchQuery = new NativeSearchQueryBuilder().withQuery(query).withIndices(contentIndex)
					.withTypes(ELASTIC_SEARCH_TYPE_ORCHESTRATOR)
					.withPageable(PageRequest.of(Default_Page, Default_size)).build();
		} else {
			searchQuery = new NativeSearchQueryBuilder().withQuery(query).withIndices(contentIndex)
					.withTypes(ELASTIC_SEARCH_TYPE_ORCHESTRATOR).withPageable(PageRequest.of(Default_Page, 10000))
					.build();
		}

		@SuppressWarnings("rawtypes")
		List<Map> map = null;
		map = elasticsearchTemplate.queryForPage(searchQuery, Map.class).getContent();

		ObjectMapper mapper = new ObjectMapper();
		String orderedResult = orderBy(mapper.writeValueAsString(map), Constants.TYPE_KEY);

		return orderedResult;
	}

	public String findSchedeByCollectionReference(String idCollezione) throws JsonProcessingException {
		BoolQueryBuilder query = QueryBuilders.boolQuery();
		QueryBuilder queryForSchedeByCollection = null;
		QueryBuilder queryCheckCollezione = null;

		queryForSchedeByCollection = QueryBuilders.termQuery(Constants.COLLECTION_VALUE_PATH, idCollezione);
		queryCheckCollezione = QueryBuilders.termQuery(Constants.CHECK_COLLECTION_VALUE_PATH, Constants.COLLEZIONE);

		query.filter(queryForSchedeByCollection);
		query.filter(queryCheckCollezione);

		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query).withIndices(contentIndex)
				.withTypes(ELASTIC_SEARCH_TYPE_ORCHESTRATOR).withPageable(PageRequest.of(Default_Page, Default_size))
				.build();

		@SuppressWarnings("rawtypes")
		List<Map> map = null;
		map = elasticsearchTemplate.queryForPage(searchQuery, Map.class).getContent();

		ObjectMapper mapper = new ObjectMapper();
		String orderedResult = orderBy(mapper.writeValueAsString(map), Constants.TYPE_KEY);
		return orderedResult;
	}

	public String orderBy(String result, String type) {
		JSONArray noSortedJsonArray = new JSONArray(result);
		JSONArray sortedJsonArray = new JSONArray();
		List<JSONObject> jsonList = new ArrayList<JSONObject>();
		for (int i = 0; i < noSortedJsonArray.length(); i++) {
			jsonList.add(noSortedJsonArray.getJSONObject(i));
		}

		if (type.equals(Constants.TYPE_KEY)) {
			Collections.sort(jsonList, new Comparator<JSONObject>() {

				public int compare(JSONObject a, JSONObject b) {
					String valA = new String();
					String valB = new String();
					try {
						valA = (String) a.getJSONObject(Constants.TYPE_PATH).get(Constants.TYPE_KEY);
						valB = (String) b.getJSONObject(Constants.TYPE_PATH).get(Constants.TYPE_KEY);
					} catch (JSONException e) {
					}
					return valA.compareTo(valB);
				}
			});
			for (int i = 0; i < noSortedJsonArray.length(); i++) {
				sortedJsonArray.put(jsonList.get(i));
			}

			return sortedJsonArray.toString();
		} else {
			Collections.sort(jsonList, new Comparator<JSONObject>() {

				public int compare(JSONObject a, JSONObject b) {
					String valA = new String();
					String valB = new String();
					try {
						valA = (String) a.getString(Constants.SCHEDA_DOMAIN);
						valB = (String) b.getString(Constants.SCHEDA_DOMAIN);
					} catch (JSONException e) {
					}

					return valA.compareTo(valB);
				}
			});
			for (int i = 0; i < noSortedJsonArray.length(); i++) {
				sortedJsonArray.put(jsonList.get(i));
			}

			return sortedJsonArray.toString();
		}
	}

}
