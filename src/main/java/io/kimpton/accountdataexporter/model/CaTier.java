package io.kimpton.accountdataexporter.model;

import java.util.List;
import lombok.Value;

@Value
public class CaTier
{
	String name;
	int completed;
	int total;
	List<CaTask> tasks;
}
