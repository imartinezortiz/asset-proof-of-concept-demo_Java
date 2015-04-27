package eu.rageproject.assets.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import eu.rageproject.assets.AssetManager;
import eu.rageproject.assets.BaseAsset;
import eu.rageproject.assets.logger.Logger;

/**
 * 
 * @author Ivan Martinez-Ortiz
 *
 */
public class DialogAsset extends BaseAsset {

	private List<Dialogue> dialogues;

	private Logger logger;

	private List<State> states;

	protected DialogAsset(String id) {
		super(id);
		this.dialogues = new ArrayList<>();
		this.states = new ArrayList<>();
		this.logger = AssetManager.getInstance().<Logger> findAssetByClass(
				"Logger");
	}

	public Dialogue interact(String actor, String player, int response) {
		return interact(actor, player, Integer.toString(response));
	}

	public Dialogue interact(String actor, String player) {
		return interact(actor, player, null);
	}

	public Dialogue interact(String actor, String player, String response) {
		int state = findStateIndex(actor, player);

		Dialogue dialogue;

		boolean numeric = true;
		try {
			Integer.parseInt(response);
		} catch (NumberFormatException nfe) {
			numeric = false;
		}

		int ndx = findIndexForDialog(actor, response);

		if (ndx != -1) {
			Dialogue responseDialogue = this.dialogues.get(ndx);

			if (numeric) {
				// If its an integer response, move the dialogue state as this
				// is a
				// response choice
				//
				if (responseDialogue.isResponse()) {
					doLog("  << %s was chosen.", responseDialogue.getId());
					state = responseDialogue.getNext();
					updateState(actor, player, state);
				}

				dialogue = firstDialogueByActorAndState(actor, state);
			} else {
				doLog("%s ask %s about %s", actor, player, response);

				//
				// ... otherwise this was a "what about the [item]" type of
				// choice
				// so we return the dialogue but don't modify the state
				//
				dialogue = responseDialogue;
			}
		} else {
			dialogue = firstDialogueByActorAndState(actor, state);
		}

		doLog("%s. %s", dialogue.getId(), dialogue.getText());

		for (int responseId : dialogue.getResponses()) {

			Dialogue answer = findDialogueById(Integer.toString(responseId));

			doLog("  >> %s. %s", answer.getId(), answer.getText());
		}

		if (dialogue.getNext() != -1) {
			updateState(actor, player, dialogue.getNext());
		}

		return dialogue;

	}

	private Dialogue findDialogueById(String dialogId) {
		for (Dialogue d : this.dialogues) {
			if (d.getId().equals(dialogId)) {
				return d;
			}
		}
		return null;
	}

	private Dialogue firstDialogueByActorAndState(String actor, int state) {
		for (Dialogue d : this.dialogues) {
			if (d.getActor().equals(actor) && d.getId().equals(state)) {
				return d;
			}
		}
		return null;
	}

	private int findStateIndex(String actor, String player) {
		int index = findIndexForState(actor, player);

		if (index == -1) {
			this.states.add(new State(actor, player, 0));
		}

		return (index != -1) ? this.states.get(index).getState() : 0;
	}

	public int findIndexForState(String actor, String player) {
		int index = 0;
		for (State s : this.states) {
			if (s.getActor().equals(actor) && s.getPlayer().equals(player)) {
				break;
			}
			index++;
		}

		if (index == this.states.size()) {
			index = -1;
		}
		return index;
	}

	private int findIndexForDialog(String actor, String response) {
		int index = 0;
		for (Dialogue d : this.dialogues) {
			if (d.getActor().equals(actor) && d.getId().equals(response)) {
				break;
			}
			index++;
		}

		if (index == this.states.size()) {
			index = -1;
		}

		return index;
	}

	private void doLog(String msg, Object... args) {
		doLog(String.format(msg, args));
	}

	private void doLog(String msg) {
		if (logger != null) {
			logger.log(msg);
		} else {
			System.out.println(msg);
		}
	}

	private void updateState(String actor, String player, int state) {
		int index = findIndexForState(actor, player);

		if (index == -1) {
			// New State
			this.states.add(new State(actor, player, state));
		} else {
			// Update State
			this.states.set(index, new State(actor, player, state));
		}
	}
}

final class Dialogue {

	private final String actor;

	private final String id;

	private final int next;

	private final List<Integer> responses;

	private final String text;

	private int cachedHashCode;

	public Dialogue(String actor, String id, int next, List<Integer> responses,
			String text) {
		this.cachedHashCode = 0;
		this.actor = actor;
		this.id = id;
		this.next = next;
		this.responses = new LinkedList<>();
		this.responses.addAll(responses);
		this.text = text;
	}

	public String getActor() {
		return actor;
	}

	public String getId() {
		return id;
	}

	public int getNext() {
		return next;
	}

	public List<Integer> getResponses() {
		return Collections.unmodifiableList(responses);
	}

	public String getText() {
		return text;
	}

	public boolean isResponse() {
		boolean isResponse = true;
		try {
			Integer.parseInt(this.id);
		} catch (NumberFormatException nfe) {
			isResponse = false;
		}
		return isResponse;
	}

	@Override
	public int hashCode() {
		if (this.cachedHashCode != 0) {
			return this.cachedHashCode;
		}

		final int prime = 31;
		int result = 1;
		result = prime * result + ((actor == null) ? 0 : actor.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + next;
		result = prime * result
				+ ((responses == null) ? 0 : responses.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());

		this.cachedHashCode = result;

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dialogue other = (Dialogue) obj;
		if (actor == null) {
			if (other.actor != null)
				return false;
		} else if (!actor.equals(other.actor))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (next != other.next)
			return false;
		if (responses == null) {
			if (other.responses != null)
				return false;
		} else if (!responses.equals(other.responses))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}
}

final class State {
	private final String actor;

	private final String player;

	private final int state;

	private int cachedHashCode;

	public State(String actor, String player, int state) {
		this.actor = actor;
		this.player = player;
		this.state = state;
	}

	public String getActor() {
		return actor;
	}

	public String getPlayer() {
		return player;
	}

	public int getState() {
		return state;
	}

	@Override
	public int hashCode() {
		if (this.cachedHashCode != 0) {
			return this.cachedHashCode;
		}

		final int prime = 31;
		int result = 1;
		result = prime * result + ((actor == null) ? 0 : actor.hashCode());
		result = prime * result + ((player == null) ? 0 : player.hashCode());
		result = prime * result + state;
		this.cachedHashCode = result;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (actor == null) {
			if (other.actor != null)
				return false;
		} else if (!actor.equals(other.actor))
			return false;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (!player.equals(other.player))
			return false;
		if (state != other.state)
			return false;
		return true;
	}

}
