package dqm.jku.dqmeerkat.connectors;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;

import dqm.jku.dqmeerkat.dsd.DSDFactory;
import dqm.jku.dqmeerkat.dsd.elements.Attribute;
import dqm.jku.dqmeerkat.dsd.elements.Concept;
import dqm.jku.dqmeerkat.dsd.elements.Datasource;
import dqm.jku.dqmeerkat.dsd.records.Record;
import dqm.jku.dqmeerkat.dsd.records.RecordList;
import dqm.jku.dqmeerkat.util.Constants;
import dqm.jku.dqmeerkat.util.Miscellaneous.DBType;
import dqm.jku.dqmeerkat.util.converters.DataTypeConverter;

import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;

/**
 * The connector for the NoSQL database Cassandra
 * 
 * @author Julia
 */
public class ConnectorCassandra extends DSConnector {

	private final Session session;
	private final Cluster cluster;
	private final String keyspace;

	/**
	 * Constructor for the Cassandra connector
	 * 
	 * @param address
	 *            IP address for the connection to the database
	 * @param keyspace
	 *            name of the investigated Cassandra's keyspace
	 */
	public ConnectorCassandra(String address, String keyspace) {
		this(address, keyspace, null, null);
	}

	public ConnectorCassandra(String address, String keyspace, String user, String pw) {
		// Store address and keyspace for further processing
		this.keyspace = keyspace;

		// Building a cluster
		Builder builder = Cluster.builder();
		builder.addContactPoint(address);
		if (user != null && pw != null) {
			builder.withCredentials(user, pw);
		}
		this.cluster = builder.build();
		this.session = cluster.connect(keyspace);
	}

	@Override
	public Datasource loadSchema(String uri, String prefix) throws IOException {
		// Create empty datasource object
		Datasource ds = DSDFactory.makeDatasource(keyspace, DBType.CASSANDRA, uri, prefix);
		// Go over all tables in the keyspace
		for (TableMetadata table : this.session.getCluster().getMetadata().getKeyspace(keyspace).getTables()) {
			// Create concept
			Concept c = DSDFactory.makeConcept(table.getName(), ds);
			// Get attributes
			loadAttributes(table, c);
		}
		return ds;
	}
	
	@Override
	public Datasource loadSchema() throws IOException {
		return loadSchema(Constants.DEFAULT_URI, Constants.DEFAULT_PREFIX);
	}

	/**
	 * Loads attributes into the DSD vocabulary/Concept c from a Cassandra table
	 * 
	 * @param table
	 * @param c
	 */
	private void loadAttributes(TableMetadata table, Concept c) {
		// Get the list of primary keys
		List<ColumnMetadata> primary = table.getPrimaryKey();
		// Go over all columns
		int i = 0;
		for (ColumnMetadata column : table.getColumns()) {
			Attribute a = DSDFactory.makeAttribute(column.getName(), c);
			// Set primary key
			if (primary.contains(column)) {
				a.setNullable(false);
				a.setUnique(true);
				c.addPrimaryKeyAttribute(a);
			} else {
				a.setNullable(true);
			}
			a.setAutoIncrement(false);
			a.setOrdinalPosition(i);
			i++;
			// Set data type
			DataTypeConverter.getTypeFromCassandra(a, column.getType());
		}
	}

	public void close() {
		cluster.close();
		session.close();
	}

	@Override
	public Iterator<Record> getRecords(Concept concept) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RecordList getRecordList(Concept concept) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNrRecords(Concept c) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public RecordList getPartialRecordList(Concept concept, int offset, int noRecords) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
