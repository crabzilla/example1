package io.github.crabzilla.example1

import io.github.crabzilla.core.AggregateRoot
import io.github.crabzilla.core.AggregateRootConfig
import io.github.crabzilla.core.AggregateRootName
import io.github.crabzilla.core.Command
import io.github.crabzilla.core.CommandHandler
import io.github.crabzilla.core.CommandHandler.ConstructorResult
import io.github.crabzilla.core.CommandValidator
import io.github.crabzilla.core.DomainEvent
import io.github.crabzilla.core.EventHandler
import io.github.crabzilla.core.Snapshot
import io.github.crabzilla.core.SnapshotTableName
import io.github.crabzilla.core.StatefulSession
import io.github.crabzilla.core.javaModule
import io.github.crabzilla.example1.UserCommand.ActivateUser
import io.github.crabzilla.example1.UserCommand.DeactivateUser
import io.github.crabzilla.example1.UserCommand.RegisterAndActivateUser
import io.github.crabzilla.example1.UserCommand.RegisterUser
import io.github.crabzilla.example1.UserEvent.UserActivated
import io.github.crabzilla.example1.UserEvent.UserDeactivated
import io.github.crabzilla.example1.UserEvent.UserRegistered
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import java.util.UUID

/**
 * User events
 */
@Serializable
sealed class UserEvent : DomainEvent() {
  @Serializable
  @SerialName("UserRegistered")
  data class UserRegistered(@Contextual val id: UUID, val name: String) : UserEvent()

  @Serializable
  @SerialName("UserActivated")
  data class UserActivated(val reason: String) : UserEvent()

  @Serializable
  @SerialName("UserDeactivated")
  data class UserDeactivated(val reason: String) : UserEvent()
}

/**
 * User commands
 */
@Serializable
sealed class UserCommand : Command() {
  @Serializable
  @SerialName("RegisterUser")
  data class RegisterUser(@Contextual val userId: UUID, val name: String) : UserCommand()

  @Serializable
  @SerialName("ActivateUser")
  data class ActivateUser(val reason: String) : UserCommand()

  @Serializable
  @SerialName("DeactivateUser")
  data class DeactivateUser(val reason: String) : UserCommand()

  @Serializable
  @SerialName("RegisterAndActivateUser")
  data class RegisterAndActivateUser(@Contextual val userId: UUID, val name: String, val reason: String) : UserCommand()
}

/**
 * User aggregate root
 */
@Serializable
@SerialName("User")
data class User(
  @Contextual val id: UUID,
  val name: String,
  val isActive: Boolean = false,
  val reason: String? = null
) : AggregateRoot() {

  companion object {
    fun create(id: UUID, name: String): ConstructorResult<User, UserEvent> {
      return ConstructorResult(User(id = id, name = name), UserRegistered(id = id, name = name))
    }
  }

  fun activate(reason: String): List<UserEvent> {
    return listOf(UserActivated(reason))
  }

  fun deactivate(reason: String): List<UserEvent> {
    return listOf(UserDeactivated(reason))
  }
}

/**
 * A command validator. You could use https://github.com/konform-kt/konform
 */
val userCmdValidator = CommandValidator<UserCommand> { command ->
  when (command) {
    is RegisterUser -> listOf()
    is RegisterAndActivateUser -> listOf()
    is ActivateUser -> listOf()
    is DeactivateUser -> listOf()
  }
}

/**
 * This function will apply an event to user state
 */
val userEventHandler = EventHandler<User, UserEvent> { state, event ->
  when (event) {
    is UserRegistered -> User.create(id = event.id, name = event.name).state
    is UserActivated -> state!!.copy(isActive = true, reason = event.reason)
    is UserDeactivated -> state!!.copy(isActive = false, reason = event.reason)
  }
}

/**
 * User errors
 */
class UserAlreadyExists(val id: UUID) : IllegalStateException("User $id already exists")

/**
 * User command handler
 */
object UserCommandHandler : CommandHandler<User, UserCommand, UserEvent> {
  override fun handleCommand(command: UserCommand, snapshot: Snapshot<User>?):
    Result<StatefulSession<User, UserEvent>> {

      return runCatching {
        when (command) {

          is RegisterUser -> {
            if (snapshot == null)
              with(User.create(id = command.userId, name = command.name), userEventHandler)
            else throw UserAlreadyExists(command.userId)
          }

          is RegisterAndActivateUser -> {
            if (snapshot == null)
              with(User.create(id = command.userId, name = command.name), userEventHandler)
                .execute { it.activate(command.reason) }
            else throw UserAlreadyExists(command.userId)
          }

          is ActivateUser -> {
            with(snapshot!!, userEventHandler)
              .execute { it.activate(command.reason) }
          }

          is DeactivateUser -> {
            with(snapshot!!, userEventHandler)
              .execute { it.deactivate(command.reason) }
          }
        }
      }
    }
}

/**
 * kotlinx.serialization
 */
@kotlinx.serialization.ExperimentalSerializationApi
val userModule = SerializersModule {
  include(javaModule)
  polymorphic(AggregateRoot::class) {
    subclass(User::class, User.serializer())
  }
  polymorphic(Command::class) {
    subclass(RegisterUser::class, RegisterUser.serializer())
    subclass(ActivateUser::class, ActivateUser.serializer())
    subclass(DeactivateUser::class, DeactivateUser.serializer())
    subclass(RegisterAndActivateUser::class, RegisterAndActivateUser.serializer())
  }
  polymorphic(DomainEvent::class) {
    subclass(UserRegistered::class, UserRegistered.serializer())
    subclass(UserActivated::class, UserActivated.serializer())
    subclass(UserDeactivated::class, UserDeactivated.serializer())
  }
}

val userJson = Json { serializersModule = userModule }

val userConfig = AggregateRootConfig(
  AggregateRootName("User"),
  SnapshotTableName("user_snapshots"),
  userEventHandler,
  userCmdValidator,
  UserCommandHandler,
  userJson
)
