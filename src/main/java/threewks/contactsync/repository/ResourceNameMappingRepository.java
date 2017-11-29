package threewks.contactsync.repository;

import contrib.springframework.data.gcp.objectify.repository.ObjectifyRepository;
import threewks.contactsync.model.ResourceNameMapping;

public interface ResourceNameMappingRepository extends ObjectifyRepository<ResourceNameMapping, String> {
}
