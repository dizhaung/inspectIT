package rocks.inspectit.shared.cs.ci.business.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.enumeration.BusinessContextErrorCodeEnum;
import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.BooleanExpression;

/**
 * Configuration element defining an application context.
 *
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "applicaction")
public class ApplicationDefinition implements IMatchingRuleProvider {
	/**
	 * The name of the default application.
	 */
	public static final String UNKNOWN_APP = "Unknown Application";

	/**
	 * The default identifier.
	 */
	public static final int DEFAULT_ID = 0;

	/**
	 * Default application definition that matches any data. Therefore {@link BooleanExpression}
	 * with true is used.
	 */
	public static final ApplicationDefinition DEFAULT_APPLICATION_DEFINITION = new ApplicationDefinition(ApplicationDefinition.DEFAULT_ID, UNKNOWN_APP, new BooleanExpression(true, true));

	/**
	 * Identifier of the application. Needs to be unique!
	 */
	@XmlAttribute(name = "id", required = true)
	private int id = (int) UUID.randomUUID().getMostSignificantBits();

	/**
	 * Name of the application.
	 */
	@XmlAttribute(name = "name", required = true)
	private String applicationName;

	/**
	 * Description.
	 */
	@XmlAttribute(name = "description", required = false)
	private String description;

	/**
	 * Revision. Server for version control and updating control.
	 */
	@JsonIgnore
	@XmlAttribute(name = "revision")
	private Integer revision = Integer.valueOf(1);

	/**
	 * Rule definition for matching measurement data to applications.
	 *
	 * Default Matching rule should not match any data, therefore {@link BooleanExpression} with
	 * false is used.
	 */
	@XmlElementRef
	private AbstractExpression matchingRuleExpression = new BooleanExpression(false);

	/**
	 * Business transaction definitions.
	 */
	@JsonIgnore
	@XmlElementWrapper(name = "business-transactions")
	@XmlElementRef(type = BusinessTransactionDefinition.class)
	private final List<BusinessTransactionDefinition> businessTransactionDefinitions = new ArrayList<BusinessTransactionDefinition>();

	/**
	 * Default constructor.
	 */
	public ApplicationDefinition() {
	}

	/**
	 * Constructor.
	 *
	 * @param applicationName
	 *            name of the application
	 */
	public ApplicationDefinition(String applicationName) {
		this.applicationName = applicationName;
	}

	/**
	 * Constructor.
	 *
	 * @param id
	 *            unique identifier to use for this {@link ApplicationDefinition}
	 * @param applicationName
	 *            name of the application
	 * @param matchingRuleExpression
	 *            matching rule to use for recognition of this application
	 */
	public ApplicationDefinition(int id, String applicationName, AbstractExpression matchingRuleExpression) {
		this(applicationName);
		this.matchingRuleExpression = matchingRuleExpression;
		this.id = id;
	}

	/**
	 * Returns the name of the application.
	 *
	 * @return Returns the name of the application
	 */
	public String getApplicationName() {
		return applicationName;
	}

	/**
	 * Sets the name of the application.
	 *
	 * @param applicationName
	 *            New value for the name of the application.
	 */
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractExpression getMatchingRuleExpression() {
		return matchingRuleExpression;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMatchingRuleExpression(AbstractExpression matchingRuleExpression) {
		this.matchingRuleExpression = matchingRuleExpression;
	}

	/**
	 * Returns an unmodifiable list of all {@link BusinessTransactionDefinition} instances known in
	 * this application definition.
	 *
	 * @return unmodifiable list of all {@link BusinessTransactionDefinition} instances known in
	 *         this application definition
	 */
	public List<BusinessTransactionDefinition> getBusinessTransactionDefinitions() {
		List<BusinessTransactionDefinition> allbusinessTxDefinitions = new ArrayList<BusinessTransactionDefinition>(businessTransactionDefinitions);
		allbusinessTxDefinitions.add(BusinessTransactionDefinition.DEFAULT_BUSINESS_TRANSACTION_DEFINITION);
		return Collections.unmodifiableList(allbusinessTxDefinitions);
	}

	/**
	 * Retrieves the {@link BusinessTransactionDefinition} with the given identifier.
	 *
	 * @param id
	 *            unique id identifying the business transaction to retrieve
	 * @return Return the {@link BusinessTransactionDefinition} with the given id, or null if no
	 *         {@link BusinessTransactionDefinition} with the passed id could be found.
	 *
	 * @throws BusinessException
	 *             if no {@link BusinessTransactionDefinition} with the given identifier exists.
	 */
	public BusinessTransactionDefinition getBusinessTransactionDefinition(int id) throws BusinessException {
		if (id == BusinessTransactionDefinition.DEFAULT_ID) {
			return BusinessTransactionDefinition.DEFAULT_BUSINESS_TRANSACTION_DEFINITION;
		}
		for (BusinessTransactionDefinition businessTxDef : businessTransactionDefinitions) {
			if (businessTxDef.getId() == id) {
				return businessTxDef;
			}
		}

		throw new BusinessException("Retrieve business transaction with id '" + id + "'.", BusinessContextErrorCodeEnum.UNKNOWN_BUSINESS_TRANSACTION);
	}

	/**
	 * Adds business transaction definition to the application definition.
	 *
	 * @param businessTransactionDefinition
	 *            {@link BusinessTransactionDefinition} instance to add
	 * @throws BusinessException
	 *             If the application definition already contains a business transaction with same
	 *             identifier.
	 */
	public void addBusinessTransactionDefinition(BusinessTransactionDefinition businessTransactionDefinition) throws BusinessException {
		addBusinessTransactionDefinition(businessTransactionDefinition, businessTransactionDefinitions.size());
	}

	/**
	 * Adds business transaction definition to the application definition. Inserts it to the list
	 * before the element with the passed index.
	 *
	 * @param businessTransactionDefinition
	 *            {@link BusinessTransactionDefinition} instance to add
	 * @param insertBeforeIndex
	 *            insert before this index
	 * @throws BusinessException
	 *             If the application definition already contains a business transaction with same
	 *             identifier or the insertBeforeIndex is not valid.
	 */
	public void addBusinessTransactionDefinition(BusinessTransactionDefinition businessTransactionDefinition, int insertBeforeIndex) throws BusinessException {
		if (businessTransactionDefinition == null) {
			throw new BusinessException("Adding business transaction 'null'.", BusinessContextErrorCodeEnum.UNKNOWN_BUSINESS_TRANSACTION);
		} else if (businessTransactionDefinitions.contains(businessTransactionDefinition)) {
			throw new BusinessException(
					"Adding business transaction " + businessTransactionDefinition.getBusinessTransactionDefinitionName() + " with id " + businessTransactionDefinition.getId() + ".",
					BusinessContextErrorCodeEnum.DUPLICATE_ITEM);
		} else if ((insertBeforeIndex < 0) || (insertBeforeIndex > businessTransactionDefinitions.size())) {
			throw new BusinessException("Adding business transaction " + businessTransactionDefinition.getBusinessTransactionDefinitionName() + " with id " + businessTransactionDefinition.getId()
			+ " at index " + insertBeforeIndex + ".", BusinessContextErrorCodeEnum.INVALID_MOVE_OPRATION);
		} else {
			businessTransactionDefinitions.add(insertBeforeIndex, businessTransactionDefinition);

		}

	}

	/**
	 * Deletes the {@link BusinessTransactionDefinition} from the application definition.
	 *
	 * @param businessTransactionDefinition
	 *            {@link BusinessTransactionDefinition} to delete
	 *
	 * @return Returns true if the application definition contained the business transaction
	 */
	public boolean deleteBusinessTransactionDefinition(BusinessTransactionDefinition businessTransactionDefinition) {
		return businessTransactionDefinitions.remove(businessTransactionDefinition);
	}

	/**
	 * Moves the {@link BusinessTransactionDefinition} to a different position specified by the
	 * index parameter.
	 *
	 * @param businessTransactionDefinition
	 *            {@link BusinessTransactionDefinition} to move
	 * @param index
	 *            position to move the {@link BusinessTransactionDefinition} to
	 * @throws BusinessException
	 *             If the moving the {@link BusinessTransactionDefinition} fails.
	 */
	public void moveBusinessTransactionDefinition(BusinessTransactionDefinition businessTransactionDefinition, int index) throws BusinessException {
		if ((index < 0) || (index >= businessTransactionDefinitions.size())) {
			throw new BusinessException("Moving business transaction to index " + index + ".", BusinessContextErrorCodeEnum.INVALID_MOVE_OPRATION);
		}

		int currentIndex = businessTransactionDefinitions.indexOf(businessTransactionDefinition);
		if (currentIndex < 0) {
			throw new BusinessException("Moving business transaction to index " + index + ".", BusinessContextErrorCodeEnum.UNKNOWN_BUSINESS_TRANSACTION);
		}

		if (index != currentIndex) {
			BusinessTransactionDefinition definitionToMove = businessTransactionDefinitions.remove(currentIndex);
			businessTransactionDefinitions.add(index, definitionToMove);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@JsonIgnore
	@Override
	public boolean isChangeable() {
		return getId() != DEFAULT_ID;
	}

	/**
	 * Returns the unique identifier of this application definition.
	 *
	 * @return Returns the unique identifier of this application definition.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the description text.
	 *
	 * @return Returns the description text.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description text.
	 *
	 * @param description
	 *            New value for the description text.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets {@link #revision}.
	 *
	 * @return {@link #revision}
	 */
	public int getRevision() {
		return revision.intValue();
	}

	/**
	 * Sets {@link #revision}.
	 *
	 * @param revision
	 *            New value for {@link #revision}
	 */
	public void setRevision(int revision) {
		this.revision = Integer.valueOf(revision);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + id;
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ApplicationDefinition other = (ApplicationDefinition) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}
}
